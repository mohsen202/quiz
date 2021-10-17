package com.wrteam.quiz.helper;


import android.content.Context;

import android.content.SharedPreferences;

public class UserSessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    public static final String PREFER_NAME = "QuizToCashPref";
    public static final String KEY_POINT = "points";
    public static final String KEY_ACTIVEQUIZ = "activequiz";
    public static final String KEY_SCORE = "score";
    public static final String KEY_QUEATTEND = "queattend";
    public static final String KEY_CORRECTANS = "currectans";
    public static final String KEY_WRONGANS = "wrongans";


    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setData(String id, String val){
        editor.putString(id, val);
        editor.commit();
    }

    public String getData(String id){
        if(id.equalsIgnoreCase(KEY_SCORE) || id.equalsIgnoreCase(KEY_QUEATTEND) || id.equalsIgnoreCase(KEY_ACTIVEQUIZ) || id.equalsIgnoreCase(KEY_CORRECTANS))
            return  pref.getString(id, "0");
        else
            return  pref.getString(id, null);
    }

}
