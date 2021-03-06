package com.example.marcin.otwieraniebramy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Map<String, Integer> activityMap;
    private PendingIntent pendingIntent;
    private LocationRequest mLocationRequest;
    private int firstLocationCounterDown;
    private int indexOfNearestLocation;
    private BroadcastReceiver receiver;
    private int countDownTimeCarDriving;
    private boolean isLocationListenerActive;
    private final List<Pair<LatLng, LatLng>> POINTS_NEAR_HOME = MyLocationPoints.points;
    private static final boolean SHOW_TOASTS = false;
    static MyService myService;
    private IBinder mBinder;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof MyUncaughtExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(getApplicationContext()));

        myService = this;
        mBinder = new LocalBinder();

        isLocationListenerActive = false;
        receiver = new ActionReceiver();
        registerReceiver(receiver, new IntentFilter(ActivityResultService.REFRESH_ACTIONS));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient.connect();

        Toast.makeText(getApplicationContext(), "aaa", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
        if (isLocationListenerActive) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            isLocationListenerActive = false;
        }
        unregisterReceiver(receiver);
        myService = null;
        Log.e("MyService", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("MyService", "onConnected");

        Toast.makeText(getApplicationContext(), "onConnected", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ActivityResultService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 5000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * sprawdzami czy jestem blisko pierwszego pkt
     * jeśli tak - sprawdzam przez następne 10 sprawdzeń czy jestem blisko drugiego
     * jeśli nie to znowu szukam pierowzego w kolejności
     * <p>
     * wyłącza się sam - jak kilka razy podczas sprawdzania aktywności samochód nie będzie jechał
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Toast.makeText(getApplicationContext(), "" + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
            if (firstLocationCounterDown == 0) {
                float minDist = 1000000;
                for (int ind = 0; ind < POINTS_NEAR_HOME.size(); ind++) {
                    Location loc = new Location("loc");
                    loc.setLatitude(POINTS_NEAR_HOME.get(ind).first.latitude);
                    loc.setLongitude(POINTS_NEAR_HOME.get(ind).first.longitude);
                    float dist = location.distanceTo(loc);

                    if (dist < 150) {
                        indexOfNearestLocation = ind;
                        firstLocationCounterDown = 15;
                        minDist = dist;
                        break;
                    } else minDist = Math.min(minDist, dist);
                }

                if (minDist > 2000)
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                else mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                if (SHOW_TOASTS)
                    Toast.makeText(this, "Odl minimalna first: " + minDist, Toast.LENGTH_SHORT).show();
            } else {
                firstLocationCounterDown--;
                Location loc = new Location("loc");

                loc.setLatitude(POINTS_NEAR_HOME.get(indexOfNearestLocation).second.latitude);
                loc.setLongitude(POINTS_NEAR_HOME.get(indexOfNearestLocation).second.longitude);
                float distanceToSecond = location.distanceTo(loc);
                if (distanceToSecond < 150) {
                    Notification.Builder builder;
                    builder = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .setContentTitle("Otwieranie bramy")
                            .setContentText("Otwieram bramę")
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setVibrate(new long[]{0, 200, 100, 200});
                    NotificationManagerCompat.from(this).notify(0, builder.build());
                    new DataHttpSenderAsyncTask(getApplicationContext(), false, true).execute();
                    firstLocationCounterDown = 0;
                } else if (SHOW_TOASTS)
                    Toast.makeText(this, "Odl minimalna second: " + distanceToSecond, Toast.LENGTH_SHORT).show();

                loc.setLatitude(POINTS_NEAR_HOME.get(indexOfNearestLocation).first.latitude);
                loc.setLongitude(POINTS_NEAR_HOME.get(indexOfNearestLocation).first.longitude);
                float distanceToFirst = location.distanceTo(loc);
                if (distanceToFirst < 150)
                    firstLocationCounterDown++;
            }
        } else
            Toast.makeText(getApplicationContext(), "nie ma lokalizacji", Toast.LENGTH_SHORT).show();
    }

    private void analyseActionDistribution() {
        Toast.makeText(getApplicationContext(), "stan", Toast.LENGTH_SHORT).show();
        if (idDeviceInVehicle() && !isLocationListenerActive)
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("MyService", "Problem with location Premission");
            } else {
                Toast.makeText(getApplicationContext(), "dokładna", Toast.LENGTH_SHORT).show();
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                isLocationListenerActive = true;
            }
        else if (!idDeviceInVehicle() && isLocationListenerActive) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            isLocationListenerActive = false;
        }
    }

    /**
     * Każde pytanie obniża licznik
     *
     * @return
     */
    private boolean idDeviceInVehicle() {
        if (activityMap != null) {
            if (activityMap.get("IN_VEHICLE") > 50) {
                countDownTimeCarDriving = 30;
                return true;
            } else if (countDownTimeCarDriving > 0) {
                countDownTimeCarDriving--;
                return true;
            }
        }

        return false;
    }

    public void getResponseFromActivityResultService(Map<String, Integer> map) {
        Toast.makeText(this, "oo", Toast.LENGTH_SHORT).show();
        activityMap = new TreeMap<>();

        for (int actInd = 0; actInd < 9; actInd++)
            if (actInd != 6)
                activityMap.put(DetectedActivity.zzjW(actInd), 0);

        for (String s : map.keySet())
            activityMap.put(s, map.get(s));

        for (String s : activityMap.keySet())
            Log.e(s, String.valueOf(activityMap.get(s)));
        Log.e("jestem", "w myservice");

        analyseActionDistribution();
    }

    private class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ActivityResultService.REFRESH_ACTIONS)) {
                activityMap = new TreeMap<>();
                for (int actInd = 0; actInd < 9; actInd++)
                    if (actInd != 6) {
                        activityMap.put(DetectedActivity.zzjW(actInd), intent.getIntExtra(DetectedActivity.zzjW(actInd), 0));
                    }
                analyseActionDistribution();
            }
        }
    }

    public class LocalBinder extends Binder {

        public MyService getMyService() {
            return MyService.this;
        }
    }
}