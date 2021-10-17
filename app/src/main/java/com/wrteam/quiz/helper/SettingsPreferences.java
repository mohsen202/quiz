package com.wrteam.quiz.helper;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsPreferences {

	public static final String SETTING_Quiz_PREF = "setting_cashquiz_pref";
	private static final String SOUND_ONOFF = "sound_enable_disable";
	private static final String SHOW_MUSIC_ONOFF = "showmusic_enable_disable";
	private static final String VIBRATION = "vibrate_status";
	public static final String IS_LAST_LEVEL_COMPLETED = "is_last_level_completed";
	public static final String FIFTY = "fifty_fifty";
	public static final String RESET = "reset";
	public static final String AUDIENCE = "audience";
	public static final String SKIP = "skip";
	public static void setVibration(Context context, Boolean result) {
		SharedPreferences prefs = context.getSharedPreferences(	SETTING_Quiz_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putBoolean(VIBRATION, result);
		prefEditor.commit();
	}
	public static boolean getVibration(Context context) {
		try {
			SharedPreferences prefs = context.getSharedPreferences(SETTING_Quiz_PREF, Context.MODE_PRIVATE);
			return prefs.getBoolean(VIBRATION, StaticUtils.DEFAULT_VIBRATION_SETTING);
		}catch (Exception e){
			e.printStackTrace();
		}
		return StaticUtils.DEFAULT_VIBRATION_SETTING;
	}
	public static void setSoundEnableDisable(Context context, Boolean result) {
		SharedPreferences prefs = context.getSharedPreferences(
				SETTING_Quiz_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putBoolean(SOUND_ONOFF, result);
		prefEditor.commit();
	}
	public static boolean getSoundEnableDisable(Context context) {
		try {
			SharedPreferences prefs = context.getSharedPreferences(SETTING_Quiz_PREF, Context.MODE_PRIVATE);
			return prefs.getBoolean(SOUND_ONOFF, StaticUtils.DEFAULT_SOUND_SETTING);
		}catch (Exception e){
			e.printStackTrace();
		}
		return StaticUtils.DEFAULT_SOUND_SETTING;
	}
	public static void setMusicEnableDisable(Context context, Boolean result) {
		SharedPreferences prefs = context.getSharedPreferences(
				SETTING_Quiz_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putBoolean(SHOW_MUSIC_ONOFF, result);
		prefEditor.commit();
	}

	public static boolean getMusicEnableDisable(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				SETTING_Quiz_PREF, Context.MODE_PRIVATE);
		return prefs.getBoolean(SHOW_MUSIC_ONOFF,
				StaticUtils.DEFAULT_MUSIC_SETTING);
	}
	public static void setOption(Context context, String option) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(option, true);
		editor.apply();
	}
	public static boolean isOptionUsed(Context context, String option) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(option, false);
	}

	public static void removeSharedPreferencesData(Context mContext) {
		if (mContext != null) {
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			if (mSharedPreferences != null) {
				mSharedPreferences.edit().remove(FIFTY).apply();
				mSharedPreferences.edit().remove(RESET).apply();
				mSharedPreferences.edit().remove(AUDIENCE).apply();
				mSharedPreferences.edit().remove(SKIP).apply();
			}
		}
	}
}
