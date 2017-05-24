package com.example.marcin.otwieraniebramy;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pavlospt.roundedletterview.RoundedLetterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.hisory_toolbar);
        myToolbar.setTitle(R.string.history_activity_name);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        new HistoryAsyncTask().execute();
    }

    void prepareList(ArrayList<HistoryRecord> records) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager pieLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(pieLayoutManager);
        HistoryRecordAdapter adapter = new HistoryRecordAdapter(records);
        progressBar.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
    }

    private class HistoryRecordAdapter extends RecyclerView.Adapter<ViewHolder> {
        ArrayList<HistoryRecord> mRecords;

        public HistoryRecordAdapter(ArrayList<HistoryRecord> records) {
            mRecords = records;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_record_view, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HistoryRecord currentRecord = mRecords.get(position);
            if (currentRecord.action == HistoryRecord.WICKET)
                holder.roundedLetterView.setTitleText(getResources().getString(R.string.wicket_letter));
            else
                holder.roundedLetterView.setTitleText(getResources().getString(R.string.gate_letter));
            holder.textViewTitle.setText(currentRecord.user);
            holder.textViewSubTitle.setText(currentRecord.date);
        }

        @Override
        public int getItemCount() {
            return mRecords.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewSubTitle;
        RoundedLetterView roundedLetterView;

        ViewHolder(View v) {
            super(v);
            roundedLetterView = (RoundedLetterView) v.findViewById(R.id.roundedLetterView);
            textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
            textViewSubTitle = (TextView) v.findViewById(R.id.textViewSubTitle);
        }
    }

    private class HistoryRecord implements Comparable<HistoryRecord> {
        public static final int GATE = 0;
        public static final int WICKET = 1;

        String date;
        String user;
        int action;
        int id;

        HistoryRecord(int id, String date, String user, int action) {
            this.id = id;
            this.date = date;
            this.action = action;
            this.user = user;
        }

        @Override
        public int compareTo(@NonNull HistoryRecord historyRecord) {
            return historyRecord.id - id;
        }

        @Override
        public String toString() {
            return "[" + id + ", " + user + ", " + date + ", " + (action == 0 ? "gate" : "wicket") + "]";
        }
    }

    private class HistoryAsyncTask extends AsyncTask<Void, Void, ArrayList<HistoryRecord>> {

        JSONArray getJSONArray(InputStream is) throws IOException, JSONException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String responseString;
            StringBuilder sb = new StringBuilder();
            while ((responseString = reader.readLine()) != null) {
                sb = sb.append(responseString);
            }
            String photoData = sb.toString();
            return new JSONArray(photoData);
        }

        ArrayList<HistoryRecord> getArrayList(JSONArray array) throws JSONException {
            ArrayList<HistoryRecord> arrayList = new ArrayList<>();
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = (JSONObject) array.get(index);
                arrayList.add(new HistoryRecord(object.optInt("Id"), object.optString("Date"),
                        object.optString("Description"),
                        (object.optInt("OpenGate") == 1 ? HistoryRecord.GATE : HistoryRecord.WICKET)));
            }
            Collections.sort(arrayList);
            return arrayList;
        }

        @Override
        protected ArrayList<HistoryRecord> doInBackground(Void... voids) {
            try {
                URL url = new URL("https://---host---/historyForPhone");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();
                return getArrayList(getJSONArray(client.getInputStream()));

            } catch (Exception e) {
                Log.e("HistoryAsyncTask", "ERROR");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryRecord> list) {
            if (list == null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Nie udało sie nawiązac połączenia", Toast.LENGTH_SHORT).show();
            } else
                prepareList(list);
        }
    }
}