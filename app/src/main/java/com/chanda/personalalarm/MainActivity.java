package com.chanda.personalalarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_ID = 111;
    public static final int PERMISSION_CODE = 6;
    public static final String CHANNEL_ID = "alarm_channel";
    public static final String CHANNEL_NAME = "alarm_notification";
    static PendingIntent pendingIntent = null;
    Button alarmOffBtn;
    Button quickAlarmBtn;
    Button setAlarmBtn;
    AlarmManager alarmManager;
    TextView txtTime;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //alarmTimePicker = findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        alarmOffBtn = findViewById(R.id.button);
        quickAlarmBtn = findViewById(R.id.button3);
        setAlarmBtn = findViewById(R.id.button2);
        txtTime = findViewById(R.id.textView);

        getPermission();
        createNotificationChannel();

        alarmOffBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                    MainActivity.this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            if(pendingIntent == pendingIntent2) {
                alarmManager.cancel(pendingIntent);
            } else {
                // Create a matching Intent
                Intent matchingIntent = new Intent(this, AlarmReceiver.class);
                matchingIntent.setAction("ACTION_SNOOZE");

// Create a matching PendingIntent
                PendingIntent matchingPendingIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        matchingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    alarmManager.cancel(matchingPendingIntent);
                }

            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);

            AlarmSoundPlayer.stopAudio();
            txtTime.setText("No alarm Set");
            String toastMessage = "Alarm Canceled";
            Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

        });

        quickAlarmBtn.setOnClickListener(v -> {
            // Quick button that sets alarm in 5 seconds
            // Will chance to 5 minutes later

            String toastMessage;
            toastMessage = "Alarm is set for 5 minutes from now";
            //toastMessage = "Alarm is set for 5 seconds from now";
            Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
            long triggerTime = System.currentTimeMillis() + (5 * 60 * 1000); // Set the alarm after 5 minutes
            //long triggerTime = System.currentTimeMillis() + (5 * 1000); // Set the alarm after 5 seconds


            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Get the AlarmManager and set the alarm
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

            setTimeText(triggerTime);

        });
        setAlarmBtn.setOnClickListener(v -> {
            //getPermission();
            // Get the current time
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create a time picker dialog
            // Default locale might switch date format
            @SuppressLint("DefaultLocale") TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    (view, hourSet, minuteSet) -> {
                        // Handle the selected time
                        setAlarm(hourSet, minuteSet);

                        setTimeText(hourSet, minuteSet);
                    }, hour, minute, false);

            // Show the time picker dialog
            timePickerDialog.show();

        });

        // Retrieve the extra value from the intent
        long timeInMillis = getIntent().getLongExtra("timeInMillis", 0);

        // Update the TextView if the extra value exists
        if (timeInMillis > 0) {
            setTimeText(timeInMillis); // Format the time as desired
        } else if (timeInMillis == -1) {
            txtTime.setText("No alarm Set");
        }
    }

    private void setTimeText(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        setTimeText(hour, minute);
    }

    @SuppressLint("DefaultLocale")
    private void setTimeText(int hour, int minute) {
        // am/pm format
        String time;
        if (hour >= 12) {
            // Convert the hour to PM format
            if (hour > 12) {
                hour -= 12;
            }
            time = String.format("Alarm Set\n%d:%02d PM", hour, minute);
        } else {
            // Convert the hour to AM format
            if (hour == 0) {
                hour = 12;
            }
            time = String.format("Alarm Set\n%d:%d AM", hour, minute);
        }
        txtTime.setText(time);
    }

    @SuppressLint("DefaultLocale")
    private void setAlarm(int hour, int minute) {
        // Create a calendar object with the selected time
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);

        // Get the current system time
        Calendar currentTime = Calendar.getInstance();

        // Check if the selected time is in the past
        if (alarmTime.before(currentTime)) {
            // Increment the alarm time by one day if it's in the past
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        long currentTimeMillis = currentTime.getTimeInMillis();

        long alarmTimeMillis = alarmTime.getTimeInMillis();
        long timeDifferenceMillis = alarmTimeMillis - currentTimeMillis;

// Convert the time difference to hours and minutes
        int hours = (int) (timeDifferenceMillis / (1000 * 60 * 60));
        int minutes = (int) ((timeDifferenceMillis / (1000 * 60)) % 60);

// Create the toast message
        String toastMessage;
        if(hours == 0){
            toastMessage = String.format("Alarm is set %d minutes from now", minutes);
        } else {
            toastMessage = String.format("Alarm is set %d hours and %d minutes from now", hours, minutes);
        }
        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

        // Create an intent for the AlarmReceiver
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager and set the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
    }
    private void getPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            getPermissionReadExternal();
        } else {
            return;
        }

    }
    private void getPermissionReadExternal() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Channel for Alarm Notifications");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "Permission is required to use ringtone", Toast.LENGTH_LONG).show();
            }
        }
    }


}