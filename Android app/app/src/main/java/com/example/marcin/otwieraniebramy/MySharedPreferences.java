package com.example.marcin.otwieraniebramy;

import android.content.Context;
import android.content.SharedPreferences;

import static android.R.id.edit;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Marcin on 28.02.2017.
 */

public class MySharedPreferences {
    private static final String PREFERENCES = "com.example.marcin.otwieraniebramy.PREFERENCES";
    private static final String LOCATION_SERVICE_IS_RUNNING = "com.example.marcin.otwieraniebramy.LOCATION_SERVICE_IS_RUNNING";

    public static void setLocationAnalise(boolean isRunning, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LOCATION_SERVICE_IS_RUNNING, isRunning);
        editor.apply();
    }

    public static boolean isLocationServiceRunning(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(LOCATION_SERVICE_IS_RUNNING, false);
    }
}
