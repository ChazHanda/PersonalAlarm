package com.chanda.personalalarm;

import static com.chanda.personalalarm.MainActivity.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class AlarmScreenActivity extends AppCompatActivity {
    // Snooze time in minutes
    private static final int SNOOZE_TIME = 3;
    private static boolean turnOff = false;
    private static PendingIntent pendingIntent = null;
    private static AlarmManager alarmManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_screen);

        Button buttonSnooze = findViewById(R.id.button_snooze);
        Button buttonTurnOff = findViewById(R.id.button_turn_off);

        buttonSnooze.setOnClickListener(v -> snooze());

        buttonTurnOff.setOnClickListener(v -> {
            turnOff = true;
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                AlarmSoundPlayer.stopAudio();
                String toastMessage = "Alarm Canceled";
                Toast.makeText(AlarmScreenActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

            } else {
                String toastMessage = "Alarm Turned Off";
                Toast.makeText(AlarmScreenActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // This will be turning off alarm sound
        AlarmSoundPlayer.stopAudio();
        Intent intent = new Intent(AlarmScreenActivity.this, MainActivity.class);
        long timeInMillis;
        if (!turnOff) {
            snooze();
            timeInMillis = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in the future
        } else {
            timeInMillis = -1;
        }

        intent.putExtra("timeInMillis", timeInMillis);

        startActivity(intent);
        //finish();
    }
    private void snooze() {
        // Snooze test value
        //long snoozeDurationInMillis = 10 * 1000; // 10 seconds
        long snoozeDurationInMillis = SNOOZE_TIME * 60 * 1000; // 5 minutes

        long triggerTime = System.currentTimeMillis() + snoozeDurationInMillis;

        // Reschedule the alarm by creating a new intent and pending intent
        Intent alarmIntent = new Intent(AlarmScreenActivity.this, AlarmReceiver.class);
        alarmIntent.setAction("ACTION_SNOOZE");
        pendingIntent = android.app.PendingIntent.getBroadcast(
                AlarmScreenActivity.this,
                0,
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

        long snoozeTime = snoozeDurationInMillis / (60 * 1000);
        int snoozeMinutes = (int) snoozeTime;
        @SuppressLint("DefaultLocale") String toastMessage = String.format("Alarm is set %d minutes from now", snoozeMinutes);

        Toast.makeText(AlarmScreenActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        finish();
    }
}
