package com.medicinereminder.app.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.DoseLogDao;
import com.medicinereminder.app.model.DoseRecord;
import com.medicinereminder.app.receiver.NotificationActionReceiver;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.DateTimeUtils;
import com.medicinereminder.app.util.NotificationHelper;

public class AlarmRingActivity extends AppCompatActivity {

    private ImageView ivIcon;
    private TextView tvMedicineName, tvDosage, tvFood;
    private MaterialButton btnTake, btnSnooze, btnDismiss;

    private Ringtone ringtone;
    private Vibrator vibrator;

    private long medicineId = -1;
    private String medicineName = "";
    private String dosage = "";
    private String form = "";
    private String foodRelation = "";
    private String timeStr = "";
    private int notificationId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn on screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm_ring);

        extractIntentExtras();
        initViews();
        startAlarmAlert();
        setupListeners();
    }

    private void extractIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            medicineId = intent.getLongExtra("medicine_id", -1);
            medicineName = intent.getStringExtra("medicine_name");
            dosage = intent.getStringExtra("dosage");
            form = intent.getStringExtra("form");
            foodRelation = intent.getStringExtra("food_relation");
            timeStr = intent.getStringExtra("time_str");
            notificationId = intent.getIntExtra("notification_id", -1);
        }
    }

    private void initViews() {
        ivIcon = findViewById(R.id.alarm_iv_icon);
        tvMedicineName = findViewById(R.id.alarm_tv_medicine_name);
        tvDosage = findViewById(R.id.alarm_tv_dosage);
        tvFood = findViewById(R.id.alarm_tv_food);
        btnTake = findViewById(R.id.alarm_btn_take);
        btnSnooze = findViewById(R.id.alarm_btn_snooze);
        btnDismiss = findViewById(R.id.alarm_btn_dismiss);

        tvMedicineName.setText(medicineName != null && !medicineName.isEmpty() ? medicineName : "Medicine");
        tvDosage.setText(dosage != null && !dosage.isEmpty() ? dosage : "Prescribed Dose");

        if (foodRelation != null && !"ANYTIME".equals(foodRelation) && !foodRelation.isEmpty()) {
            if ("BEFORE_MEAL".equals(foodRelation)) tvFood.setText("Take Before Meal");
            else if ("AFTER_MEAL".equals(foodRelation)) tvFood.setText("Take After Meal");
            else if ("WITH_MEAL".equals(foodRelation)) tvFood.setText("Take With Meal");
        } else {
            tvFood.setText("Anytime / No Food Restriction");
        }

        if (form != null) {
            switch (form.toLowerCase()) {
                case "capsule": ivIcon.setImageResource(R.drawable.ic_form_capsule); break;
                case "syrup":
                case "liquid": ivIcon.setImageResource(R.drawable.ic_form_syrup); break;
                case "injection": ivIcon.setImageResource(R.drawable.ic_form_injection); break;
                case "drops": ivIcon.setImageResource(R.drawable.ic_form_drops); break;
                case "inhaler": ivIcon.setImageResource(R.drawable.ic_form_inhaler); break;
                default: ivIcon.setImageResource(R.drawable.ic_form_tablet); break;
            }
        }
    }

    private void startAlarmAlert() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }
                ringtone.play();
            }
        } catch (Exception ignored) {}

        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 600, 300, 600, 300, 600};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception ignored) {}
    }

    private void stopAlarmAlert() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception ignored) {}

        try {
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        btnTake.setOnClickListener(v -> {
            stopAlarmAlert();
            if (notificationId != -1) {
                NotificationHelper.cancelNotification(this, notificationId);
            }

            // Log dose in SQLite
            String today = DateTimeUtils.getTodayDateString();
            long scheduledMillis = DateTimeUtils.getMillisForDateTime(today, timeStr != null ? timeStr : "08:00");

            DoseRecord record = new DoseRecord();
            record.setMedicineId(medicineId);
            record.setMedicineName(medicineName);
            record.setDosage(dosage);
            record.setForm(form);
            record.setFoodRelation(foodRelation);
            record.setScheduledTimeMillis(scheduledMillis);
            record.setActualTimeMillis(System.currentTimeMillis());
            record.setStatus(DoseRecord.STATUS_TAKEN);
            record.setDateStr(today);
            record.setTimeStr(timeStr != null ? timeStr : DateTimeUtils.formatDate(System.currentTimeMillis(), "HH:mm"));
            record.setNotes("Taken from alarm screen");

            DoseLogDao dao = new DoseLogDao(this);
            dao.insertOrUpdateLog(record);

            // Broadcast UI update
            sendBroadcast(new Intent(NotificationActionReceiver.ACTION_DOSE_UPDATED));

            Toast.makeText(this, "Dose marked as taken!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            stopAlarmAlert();
            if (notificationId != -1) {
                NotificationHelper.cancelNotification(this, notificationId);
            }

            AlarmScheduler.scheduleSnooze(this, medicineId, medicineName, dosage, form, foodRelation, timeStr, 10);
            Toast.makeText(this, "Reminder snoozed for 10 minutes.", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnDismiss.setOnClickListener(v -> {
            stopAlarmAlert();
            if (notificationId != -1) {
                NotificationHelper.cancelNotification(this, notificationId);
            }
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmAlert();
    }
}
