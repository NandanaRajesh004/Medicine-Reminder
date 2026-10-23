package com.medicinereminder.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    /**
     * Schedule alarms for all times defined in a medicine
     */
    public static void scheduleMedicineAlarms(Context context, Medicine medicine) {
        if (medicine == null || !medicine.isActive()) {
            return;
        }

        List<String> times = medicine.getTimesList();
        for (int i = 0; i < times.size(); i++) {
            String timeStr = times.get(i);
            scheduleSingleAlarm(context, medicine, timeStr, i);
        }
    }

    /**
     * Schedule alarm for a single time slot of a medicine
     */
    public static void scheduleSingleAlarm(Context context, Medicine medicine, String timeStr, int timeIndex) {
        int[] hm = DateTimeUtils.parseHourMinute(timeStr);
        int hour = hm[0];
        int minute = hm[1];

        // Find the next upcoming timestamp for this medicine
        Calendar nextTrigger = Calendar.getInstance();
        nextTrigger.set(Calendar.HOUR_OF_DAY, hour);
        nextTrigger.set(Calendar.MINUTE, minute);
        nextTrigger.set(Calendar.SECOND, 0);
        nextTrigger.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();

        // If today's time is already past or today is not a scheduled day, scan upcoming days
        if (nextTrigger.getTimeInMillis() <= now || !medicine.isScheduledForDay(nextTrigger)) {
            boolean found = false;
            // Search next 365 days for the next valid occurrence
            for (int d = 1; d <= 365; d++) {
                Calendar testCal = Calendar.getInstance();
                testCal.add(Calendar.DAY_OF_YEAR, d);
                testCal.set(Calendar.HOUR_OF_DAY, hour);
                testCal.set(Calendar.MINUTE, minute);
                testCal.set(Calendar.SECOND, 0);
                testCal.set(Calendar.MILLISECOND, 0);

                if (medicine.isScheduledForDay(testCal)) {
                    nextTrigger = testCal;
                    found = true;
                    break;
                }
            }
            if (!found) {
                Log.d(TAG, "No future scheduled days found for medicine: " + medicine.getName());
                return;
            }
        }

        long triggerMillis = nextTrigger.getTimeInMillis();
        int requestCode = getRequestCode(medicine.getId(), timeIndex);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicine_id", medicine.getId());
        intent.putExtra("medicine_name", medicine.getName());
        intent.putExtra("dosage", medicine.getDosage());
        intent.putExtra("form", medicine.getForm());
        intent.putExtra("food_relation", medicine.getFoodRelation());
        intent.putExtra("time_str", timeStr);
        intent.putExtra("time_index", timeIndex);
        intent.putExtra("request_code", requestCode);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            }
            Log.d(TAG, "Scheduled alarm for " + medicine.getName() + " at " + nextTrigger.getTime() + " (reqCode: " + requestCode + ")");
        } catch (SecurityException se) {
            // Android 12+ exact alarm permission fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            Log.w(TAG, "Exact alarm permission missing, fell back to standard alarm: " + se.getMessage());
        }
    }

    /**
     * Cancel all alarms scheduled for a medicine
     */
    public static void cancelMedicineAlarms(Context context, Medicine medicine) {
        if (medicine == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        List<String> times = medicine.getTimesList();
        for (int i = 0; i < Math.max(times.size(), 10); i++) {
            int requestCode = getRequestCode(medicine.getId(), i);
            Intent intent = new Intent(context, AlarmReceiver.class);
            int flags = PendingIntent.FLAG_NO_CREATE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    /**
     * Schedule a snooze alarm (e.g. 10 minutes later)
     */
    public static void scheduleSnooze(Context context, long medicineId, String medicineName,
                                      String dosage, String form, String foodRelation, String timeStr, int snoozeMinutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L);
        int requestCode = (int) (medicineId * 100 + 99); // 99 reserved for snooze

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicine_id", medicineId);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("dosage", dosage);
        intent.putExtra("form", form);
        intent.putExtra("food_relation", foodRelation);
        intent.putExtra("time_str", timeStr);
        intent.putExtra("time_index", 99);
        intent.putExtra("request_code", requestCode);
        intent.putExtra("is_snooze", true);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            }
            Log.d(TAG, "Snoozed alarm for " + medicineName + " for " + snoozeMinutes + " mins");
        } catch (SecurityException se) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        }
    }

    /**
     * Reschedule all active medicine alarms (e.g. after phone reboot or database updates)
     */
    public static void rescheduleAllAlarms(Context context) {
        MedicineDao dao = new MedicineDao(context);
        List<Medicine> activeMedicines = dao.getActiveMedicines();
        for (Medicine medicine : activeMedicines) {
            scheduleMedicineAlarms(context, medicine);
        }
        Log.d(TAG, "Rescheduled alarms for " + activeMedicines.size() + " active medicines.");
    }

    public static int getRequestCode(long medicineId, int timeIndex) {
        return (int) (medicineId * 100 + (timeIndex % 100));
    }
}
