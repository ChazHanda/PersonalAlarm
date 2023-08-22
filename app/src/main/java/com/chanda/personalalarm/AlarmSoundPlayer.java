package com.chanda.personalalarm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

public class AlarmSoundPlayer {

    public static MediaPlayer mediaPlayer;

    public static void playAudio(Context context){



        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        context.grantUriPermission(context.getPackageName(), ringtoneUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mediaPlayer = MediaPlayer.create(context, ringtoneUri);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build();
        mediaPlayer.setAudioAttributes(attributes);
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
    public static void stopAudio(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

    }
}
