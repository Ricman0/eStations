package it.univaq.estations.activity;

import android.os.Bundle;
import android.widget.Adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.VolleyRequest;

public class StationsList extends AppCompatActivity {

    private List<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_list);

        recyclerView = findViewById(R.id.stations_list);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        StationsListAdapter mAdapter = new StationsListAdapter();
    }

    private void downloadData() {

        VolleyRequest.getInstance(getApplicationContext())
                .downloadStations(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonRoot = new JSONArray(response);
                            for (int i = 0; i < jsonRoot.length(); i++) {

                                JSONObject item = jsonRoot.getJSONObject(i);

                                String name = item.getString("name");
                                String region = item.getString("region");

                                final Station city = new Station();
                                city.setName(name);

                                // Save on Database every city
                                //Database.getInstance(getApplicationContext()).save(city);

//                                new Thread(new Runnable() {
//                                    public void run() {
//                                        RDatabase.getInstance(getApplicationContext())
//                                                .getCityDao().save(city);
//                                    }
//                                }).start();
                                stations.add(city);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Refresh list because the adapter data are changed
//                        if(adapter != null) adapter.notifyDataSetChanged();
                    }
                });
    }
}
