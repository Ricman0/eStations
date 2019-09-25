package it.univaq.estations.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.RequestService;
import it.univaq.estations.utility.Settings;
import it.univaq.estations.utility.VolleyRequest;

public class StationsList extends AppCompatActivity {

    private List<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_list);

        recyclerView = findViewById(R.id.stations_list);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        StationsListAdapter adapter = new StationsListAdapter();
    }

    // The Broadcast Receiver can receive the sent intent by LocaleBroadcastManager.
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null) return;
            String response = intent.getStringExtra("response");
            if(response == null) return;

            // Parsing of JSON response:
            // [{"id":0,"name":"Ancona","region":"Marche","lat":43.615,"lon":13.515}, ...]
            try {
                JSONArray jsonRoot = new JSONArray(response);
                for(int i = 0; i < jsonRoot.length(); i++){

                    JSONObject item = jsonRoot.getJSONObject(i);

                    String id = item.getString("ID");

                    JSONObject addressInfo = item.getJSONObject("AddressInfo");

                    String title = addressInfo.getString("Title");

                    String address = addressInfo.getString("AddressLine1");

                    String town = addressInfo.getString("Town");

                    String stateOrProvince = addressInfo.getString("StateOrProvince");

                    LatLng position = new LatLng(addressInfo.getDouble("Latitude"), addressInfo.getDouble("Longitude"));

                    String url = addressInfo.getString("RelatedURL");

                    int numberOfConnections = item.getInt("NumberOfPoints");

                    JSONArray connections = item.getJSONArray("Connections");

                    Station station = new Station(id, title, address, town, stateOrProvince, position, url, numberOfConnections);

                    for (int j = 0; j < numberOfConnections; j++)
                    {
                        JSONObject connection = connections.getJSONObject(j);
                        PointOfCharge pointOfCharge = new PointOfCharge(
                                connection.getInt("ID"), connection.getInt("Voltage"),
                                connection.getInt("PowerKW"), connection.getInt("StatusTypeID")
                        );
                        station.addPointOfCharge(pointOfCharge);
                    }


//                    // Save on Database every city
//                    Database.getInstance(getApplicationContext()).save(city);
//                    cities.add(city);
                }

            } catch (JSONException e){
                e.printStackTrace();
            }

            // Refresh list because the adapter data are changed
            if(adapter != null) adapter.notifyDataSetChanged();
        }
    };

    protected void onResume() {
        super.onResume();

        // Registering the receiver
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(myReceiver, new IntentFilter(RequestService.FILTER_REQUEST_DOWNLOAD));

        // If is the first time you open the app, do a HTTP request to download the data
//        if(Settings.loadBoolean(getApplicationContext(), Settings.FIRST_TIME, true)){
//            Intent intentService = new Intent(getApplicationContext(), RequestService.class);
//            intentService.putExtra(RequestService.REQUEST_ACTION, RequestService.REQUEST_DOWNLOAD);
//            startService(intentService);
            downloadData();

//        }
//        else {
//            // If is not the first time you open the app, get all saved data from Database
//            stations.addAll(Database.getInstance(getApplicationContext()).getAllCities());
//            if(adapter != null) adapter.notifyDataSetChanged();
//
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    List<City> data = RDatabase.getInstance(getApplicationContext())
//                            .getCityDao().getAllCities();
//                    cities.addAll(data);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(adapter != null) adapter.notifyDataSetChanged();
//                        }
//                    });
//                }
//            }).start();
//
//
//        }
        Settings.save(getApplicationContext(), Settings.FIRST_TIME, false);
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
