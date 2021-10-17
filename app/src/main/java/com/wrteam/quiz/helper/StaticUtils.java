package com.wrteam.quiz.helper;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.wrteam.quiz.R;


public class StaticUtils {
    private static Vibrator sVibrator;
    public static final long VIBRATION_DURATION = 100;
    public static int RequestlevelNo = 1;
    public static final boolean DEFAULT_SOUND_SETTING = true;
    public static final boolean DEFAULT_VIBRATION_SETTING = true;
    public static final boolean DEFAULT_MUSIC_SETTING = false;
    public static final boolean DEFAULT_LAN_SETTING = true;
    public static MediaPlayer mediaplayer;


    public static void setrightAnssound(Context mContext) {
        try {
            int resourceId = R.raw.right;
            MediaPlayer mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setwronAnssound(Context mContext) {
        try {
            int resourceId = R.raw.wrong;
            MediaPlayer mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setimer(Context mContext) {
        try {
            int resourceId = R.raw.timer;
            mediaplayer=null;
            mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.setLooping(true);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stoptimer() {
        if (mediaplayer != null && mediaplayer.isPlaying()) {
            mediaplayer.stop();
            mediaplayer=null;
            //mediaplayer.release();
        }
    }

    public static void vibrate(Context context, long duration) {
        if (sVibrator == null) {
            sVibrator = (Vibrator) context
                    .getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (sVibrator != null) {
            if (duration == 0) {
                duration = 50;
            }
            sVibrator.vibrate(duration);
        }
    }
}
