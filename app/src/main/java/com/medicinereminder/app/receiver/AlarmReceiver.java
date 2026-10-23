package com.medicinereminder.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.ui.AlarmRingActivity;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra("medicine_id", -1);
        String medicineName = intent.getStringExtra("medicine_name");
        String dosage = intent.getStringExtra("dosage");
        String form = intent.getStringExtra("form");
        String foodRelation = intent.getStringExtra("food_relation");
        String timeStr = intent.getStringExtra("time_str");
        int timeIndex = intent.getIntExtra("time_index", 0);
        boolean isSnooze = intent.getBooleanExtra("is_snooze", false);

        Log.d(TAG, "Alarm received for medicine: " + medicineName + " at " + timeStr);

        // Wake lock to ensure CPU remains on long enough to show notification and launch screen
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MedicineReminder:AlarmWakeLock");
            wakeLock.acquire(10 * 1000L); // 10 seconds timeout
        }

        try {
            // Post notification with action buttons and full screen intent
            NotificationHelper.showMedicineNotification(context, medicineId, medicineName, dosage, form, foodRelation, timeStr);

            // Attempt to launch the full-screen alarm screen if screen is off or in background
            try {
                Intent ringIntent = new Intent(context, AlarmRingActivity.class);
                ringIntent.putExtra("medicine_id", medicineId);
                ringIntent.putExtra("medicine_name", medicineName);
                ringIntent.putExtra("dosage", dosage);
                ringIntent.putExtra("form", form);
                ringIntent.putExtra("food_relation", foodRelation);
                ringIntent.putExtra("time_str", timeStr);
                ringIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(ringIntent);
            } catch (Exception e) {
                Log.w(TAG, "Could not start full-screen activity directly: " + e.getMessage());
            }

            // If this was not a one-off snooze alarm, schedule the next recurring occurrence!
            if (!isSnooze && medicineId > 0) {
                MedicineDao dao = new MedicineDao(context);
                Medicine med = dao.getMedicineById(medicineId);
                if (med != null && med.isActive()) {
                    AlarmScheduler.scheduleSingleAlarm(context, med, timeStr, timeIndex);
                }
            }
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
