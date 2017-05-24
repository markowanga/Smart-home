package com.example.marcin.otwieraniebramy;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityResultService extends IntentService {

    public static final String REFRESH_ACTIONS = "com.example.marcin.otwieraniebramy.action.REFRESH_ACTIONS";

    public ActivityResultService() {
        super("ActivityResultService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("ActivityResultService", "running");
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Intent broadcastIntent = new Intent(REFRESH_ACTIONS);

        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e("ActivityRecognition", "In Vehicle: " + activity.getConfidence());
                    broadcastIntent.putExtra("IN_VEHICLE", activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e("ActivityRecognition", "On Bicycle: " + activity.getConfidence());
                    broadcastIntent.putExtra("ON_BICYCLE", activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e("ActivityRecognition", "On Foot: " + activity.getConfidence());
                    broadcastIntent.putExtra("ON_FOOT", activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e("ActivityRecognition", "Running: " + activity.getConfidence());
                    broadcastIntent.putExtra("RUNNING", activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e("ActivityRecognition", "Still: " + activity.getConfidence());
                    broadcastIntent.putExtra("STILL", activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e("ActivityRecognition", "Tilting: " + activity.getConfidence());
                    broadcastIntent.putExtra("TILTING", activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e("ActivityRecognition", "Walking: " + activity.getConfidence());
                    broadcastIntent.putExtra("WALKING", activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e("ActivityRecognition", "Unknown: " + activity.getConfidence());
                    broadcastIntent.putExtra("UNKNOWN", activity.getConfidence());
                    break;
                }
            }
        }

        sendBroadcast(broadcastIntent);
    }
}