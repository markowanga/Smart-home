package com.example.marcin.otwieraniebramy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemStartReceiver extends BroadcastReceiver {


    public SystemStartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("SystemStartReceiver", "" + MySharedPreferences.isLocationServiceRunning(context));
        if (MySharedPreferences.isLocationServiceRunning(context))
            context.startService(new Intent(context, MyService.class));
    }
}
