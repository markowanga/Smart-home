package com.example.marcin.otwieraniebramy;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Marcin on 21.02.2017.
 *
 * Class to send data to server
 */

public class DataHttpSenderAsyncTask extends AsyncTask<Void, Void, String> {
    private final Context mContext;
    private boolean mOpenGate, mOpenWicket;

    /**
     * Constructor
     * @param context - application context, use to get IMEI and make Toast
     * @param openWicket - inform that Wicket should be open
     * @param openGate - inform that Gate should be open
     */
    DataHttpSenderAsyncTask(Context context, boolean openWicket, boolean openGate) {
        mContext = context;
        mOpenGate = openGate;
        mOpenWicket = openWicket;
    }

    /**
     * Send the request at the server
     * @param voids - nothing here
     * @return - return the response message from request
     */
    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("https://---host---/mobileController?wicket="
                    + String.valueOf(mOpenWicket) + "&gate=" + String.valueOf(mOpenGate)
                    + "&&IMEI=" + getIMEI());
            HttpsURLConnection client = (HttpsURLConnection) url.openConnection();
            Log.v("DataHttpSenderAsyncTask", "Response code: " + client.getResponseCode());
            return client.getResponseMessage();
        } catch (Exception e) {
            Log.e("DataHttpSenderAsyncTask", "ERROR");
            e.printStackTrace();
            return "Error during sending data to server";
        }
    }

    /**
     * Gives IMEI
     * @return IMEI
     */
    private String getIMEI() {
        return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
    }

    /**
     * Makes toast with code respond
     * @param response - returned from doInBackground
     */
    @Override
    protected void onPostExecute(String response) {
        Toast.makeText(mContext, mContext.getString(R.string.httpSenderToastResponse) + response, Toast.LENGTH_SHORT).show();
    }
}