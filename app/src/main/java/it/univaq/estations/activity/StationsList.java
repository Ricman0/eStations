package it.univaq.estations.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    private ArrayList<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;
    //per la posizione
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // downloadData();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_stations_list);

        recyclerView = (RecyclerView) findViewById(R.id.stations_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



        // specify an adapter (see also next example)
        //mAdapter = new MyAdapter(myDataset);
        //recyclerView.setAdapter(mAdapter);
        adapter = new StationsListAdapter(this, stations);
        recyclerView.setAdapter(adapter);

    }

    // The Broadcast Receiver can receive the sent intent by LocaleBroadcastManager.
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null) return;
            String response = intent.getStringExtra("response");
            if(response == null) return;

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


    private void downloadData()
    {
        int v;

        fusedLocationClient.getLastLocation().getResult().getLatitude();

        int y;
        LatLng x = new LatLng(        fusedLocationClient.getLastLocation().getResult().getLatitude()
                ,        fusedLocationClient.getLastLocation().getResult().getLongitude());

        VolleyRequest.getInstance(getApplicationContext())
                .downloadStations(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonRoot = new JSONArray(response);
                            for (int i = 0; i < jsonRoot.length(); i++) {

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

                                for (int j = 0; j < numberOfConnections-1; j++)
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

                                stations.add(station);

//                                new Thread(new Runnable() {
//                                    public void run() {
//                                        RDatabase.getInstance(getApplicationContext())
//                                                .getCityDao().save(city);
//                                    }
//                                }).start();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Refresh list because the adapter data are changed
                        if(adapter != null) adapter.notifyDataSetChanged();
                    }
                }, x);
    }
}
