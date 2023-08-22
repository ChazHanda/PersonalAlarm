package com.chanda.personalalarm;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 111;


    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {


        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(4000, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(vibrationEffect);


        Context appContext = context.getApplicationContext();

        // Obtain the PowerManager and KeyguardManager instances
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);


// Acquire a wake lock to wake up the screen
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "myapp:WakeLockForAlarm"
        );
        // 5 minutes
        wakeLock.acquire(5  * 60 * 1000L);


        AlarmSoundPlayer.playAudio(appContext);

        // Create the intent to open the AlarmScreenActivity
        Intent alarmScreenIntent = new Intent(context, AlarmScreenActivity.class);
        alarmScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the pending intent for the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                alarmScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Alarm Notification")
                .setContentText("Your alarm is ringing!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        wakeLock.release();
    }

}

