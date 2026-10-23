package com.medicinereminder.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.medicinereminder.app.database.DoseLogDao;
import com.medicinereminder.app.model.DoseRecord;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.DateTimeUtils;
import com.medicinereminder.app.util.NotificationHelper;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_TAKE = "com.medicinereminder.app.ACTION_TAKE";
    public static final String ACTION_SNOOZE = "com.medicinereminder.app.ACTION_SNOOZE";
    public static final String ACTION_DOSE_UPDATED = "com.medicinereminder.app.ACTION_DOSE_UPDATED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        long medicineId = intent.getLongExtra("medicine_id", -1);
        String medicineName = intent.getStringExtra("medicine_name");
        String dosage = intent.getStringExtra("dosage");
        String form = intent.getStringExtra("form");
        String foodRelation = intent.getStringExtra("food_relation");
        String timeStr = intent.getStringExtra("time_str");
        int notificationId = intent.getIntExtra("notification_id", -1);

        // Cancel the notification
        if (notificationId != -1) {
            NotificationHelper.cancelNotification(context, notificationId);
        }

        if (ACTION_TAKE.equals(action)) {
            String today = DateTimeUtils.getTodayDateString();
            if (timeStr == null || timeStr.isEmpty()) {
                timeStr = DateTimeUtils.formatDate(System.currentTimeMillis(), "HH:mm");
            }
            long scheduledMillis = DateTimeUtils.getMillisForDateTime(today, timeStr);

            DoseRecord record = new DoseRecord();
            record.setMedicineId(medicineId);
            record.setMedicineName(medicineName != null ? medicineName : "Medicine");
            record.setDosage(dosage != null ? dosage : "");
            record.setForm(form);
            record.setFoodRelation(foodRelation);
            record.setScheduledTimeMillis(scheduledMillis);
            record.setActualTimeMillis(System.currentTimeMillis());
            record.setStatus(DoseRecord.STATUS_TAKEN);
            record.setDateStr(today);
            record.setTimeStr(timeStr);
            record.setNotes("Taken via reminder notification");

            DoseLogDao dao = new DoseLogDao(context);
            dao.insertOrUpdateLog(record);

            Toast.makeText(context, "Dose logged: " + medicineName + " (" + dosage + ") marked as taken!", Toast.LENGTH_LONG).show();

            // Notify UI
            Intent updateIntent = new Intent(ACTION_DOSE_UPDATED);
            context.sendBroadcast(updateIntent);

        } else if (ACTION_SNOOZE.equals(action)) {
            AlarmScheduler.scheduleSnooze(context, medicineId, medicineName, dosage, form, foodRelation, timeStr, 10);
            Toast.makeText(context, "Reminder snoozed for 10 minutes.", Toast.LENGTH_SHORT).show();
        }
    }
}
