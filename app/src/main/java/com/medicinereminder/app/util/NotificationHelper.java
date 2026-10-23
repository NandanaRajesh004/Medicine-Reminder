package com.medicinereminder.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.medicinereminder.app.R;
import com.medicinereminder.app.receiver.NotificationActionReceiver;
import com.medicinereminder.app.ui.AlarmRingActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID = "medicine_reminder_channel_v2";
    public static final String CHANNEL_NAME = "Medicine Reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts and notifications for scheduled medicines");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            channel.setSound(alarmSound, audioAttributes);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showMedicineNotification(Context context, long medicineId, String medicineName,
                                                String dosage, String form, String foodRelation, String timeStr) {
        createNotificationChannel(context);

        int notificationId = (int) (medicineId * 100 + (timeStr != null ? timeStr.hashCode() % 100 : 0));
        if (notificationId < 0) notificationId = -notificationId;

        // Content Intent: Open Alarm Ring Screen
        Intent fullScreenIntent = new Intent(context, AlarmRingActivity.class);
        fullScreenIntent.putExtra("medicine_id", medicineId);
        fullScreenIntent.putExtra("medicine_name", medicineName);
        fullScreenIntent.putExtra("dosage", dosage);
        fullScreenIntent.putExtra("form", form);
        fullScreenIntent.putExtra("food_relation", foodRelation);
        fullScreenIntent.putExtra("time_str", timeStr);
        fullScreenIntent.putExtra("notification_id", notificationId);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Action: Mark Taken
        Intent takeIntent = new Intent(context, NotificationActionReceiver.class);
        takeIntent.setAction(NotificationActionReceiver.ACTION_TAKE);
        takeIntent.putExtra("medicine_id", medicineId);
        takeIntent.putExtra("medicine_name", medicineName);
        takeIntent.putExtra("dosage", dosage);
        takeIntent.putExtra("form", form);
        takeIntent.putExtra("food_relation", foodRelation);
        takeIntent.putExtra("time_str", timeStr);
        takeIntent.putExtra("notification_id", notificationId);

        PendingIntent takePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 1,
                takeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Action: Snooze
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(NotificationActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("medicine_id", medicineId);
        snoozeIntent.putExtra("medicine_name", medicineName);
        snoozeIntent.putExtra("dosage", dosage);
        snoozeIntent.putExtra("form", form);
        snoozeIntent.putExtra("food_relation", foodRelation);
        snoozeIntent.putExtra("time_str", timeStr);
        snoozeIntent.putExtra("notification_id", notificationId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 2,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String title = "Time to take " + (medicineName != null ? medicineName : "Medicine") + "!";
        String contentText = (dosage != null ? dosage : "") +
                (foodRelation != null && !foodRelation.isEmpty() && !"ANYTIME".equals(foodRelation) ?
                        " (" + formatFood(foodRelation) + ")" : "");

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_medicines)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText + "\nTap to view dose details or take it now."))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
                .addAction(R.drawable.ic_check, "Mark as Taken", takePendingIntent)
                .addAction(R.drawable.ic_snooze, "Snooze (10m)", snoozePendingIntent);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException ignored) {
            // Android 13+ permission may not be granted yet
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }
    }

    private static String formatFood(String foodRelation) {
        if ("BEFORE_MEAL".equals(foodRelation)) return "Before Meal";
        if ("AFTER_MEAL".equals(foodRelation)) return "After Meal";
        if ("WITH_MEAL".equals(foodRelation)) return "With Meal";
        return "Anytime";
    }
}
