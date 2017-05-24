package com.example.marcin.otwieraniebramy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button mapButton, locationAnalyzeButton, openGateButton, openWicketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof MyUncaughtExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(getApplicationContext()));

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mapButton = (Button) findViewById(R.id.buttonMap);
        locationAnalyzeButton = (Button) findViewById(R.id.buttonStartScanning);
        openGateButton = (Button) findViewById(R.id.buttonOpenGate);
        openWicketButton = (Button) findViewById(R.id.buttonOpenWricket);

        // set good label on locationAnalyzeButton
        if (!isServiceRunning(MyService.class))
            locationAnalyzeButton.setText(R.string.startAnalyzeLocation);
        else locationAnalyzeButton.setText(R.string.stopAnalyzeLocation);


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // throw new Error("test error");
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

        locationAnalyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isServiceRunning(MyService.class)) {
                    startService(new Intent(getApplicationContext(), MyService.class));
                    Toast.makeText(getApplicationContext(), "Start analizy", Toast.LENGTH_SHORT).show();
                    locationAnalyzeButton.setText(R.string.stopAnalyzeLocation);
                    MySharedPreferences.setLocationAnalise(true, getApplicationContext());
                } else {
                    stopService(new Intent(getApplicationContext(), MyService.class));
                    Log.e("main", "koniec");
                    Toast.makeText(getApplicationContext(), "Koniec analizy", Toast.LENGTH_SHORT).show();
                    locationAnalyzeButton.setText(R.string.startAnalyzeLocation);
                    MySharedPreferences.setLocationAnalise(false, getApplicationContext());
                }
            }
        });

        openGateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DataHttpSenderAsyncTask(getApplicationContext(), false, true).execute();
            }
        });

        openWicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DataHttpSenderAsyncTask(getApplicationContext(), true, false).execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_show_history: {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                return true;
            }
            case R.id.action_show_map: {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;

        return false;
    }
}