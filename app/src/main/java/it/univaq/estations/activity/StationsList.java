package it.univaq.estations.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.Settings;
import it.univaq.estations.utility.VolleyRequest;
import it.univaq.estations.utility.PermissionService;

public class StationsList extends AppCompatActivity {

    private ArrayList<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;
    //per la posizione
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentPos;
    private Database appDB;
    private boolean shouldExecuteDownload;
    Handler mHandler;
    Thread threadToLoadAllStationsFromDB;
    private static final int LOAD_STATIONS_COMPLETED = 101;
    private static final int ALL_STATIONS_SAVED = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldExecuteDownload = true;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_stations_list);

        recyclerView = findViewById(R.id.stations_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new StationsListAdapter(this, stations);
        recyclerView.setAdapter(adapter);
        appDB = Database.getInstance(getApplicationContext());

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == LOAD_STATIONS_COMPLETED) {
                    //
                }
                if(msg.what == ALL_STATIONS_SAVED)
                {
                    //
                }
            }
        };

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
        PermissionService.getInstance().permissionsCheck(this, this);

        if(shouldExecuteDownload) {
            shouldExecuteDownload = false;
            // se fusedLocationClient.getLastLocation() == l'ultima posizione memorizzata allora recupero dal db altimenti richiedo; cancello e memorizzo nuove stazioni.
            boolean location_updated = Settings.loadBoolean(getApplicationContext(), Settings.LOCATION_UPDATED, true);
            if (location_updated == true) {
                stations = new ArrayList<>();
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    //delete all database table
                                    //                                appDB.clearAllTables(); // è asincrono

                                    currentPos = new LatLng(location.getLatitude(), location.getLongitude());

                                    // Registering the receiver
                                    //                    LocalBroadcastManager.getInstance(getApplicationContext())
                                    //                            .registerReceiver(myReceiver, new IntentFilter(RequestService.FILTER_REQUEST_DOWNLOAD));
                                    downloadData();
                                   // Settings.save(getApplicationContext(), Settings.LOCATION_UPDATED, false);
                                    Settings.save(getApplicationContext(), Settings.LOCATION_UPDATED, true);
                                }
                            }
                        });
            }
            else {
                Settings.save(getApplicationContext(), Settings.LOCATION_UPDATED, true);
                threadToLoadAllStationsFromDB = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stations = new ArrayList<>();
                        //get stations and all pointOFCharges from database
                        stations.addAll(appDB.getStationDao().getAllStations()); // get all stations without theirs pointOFCharges
                        for (int k = 0; k < stations.size(); k++) {
                            Station stationToFill = stations.get(k);
                            stationToFill.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationToFill.getId()));
                        }
                        Message message = new Message();
                        message.what = LOAD_STATIONS_COMPLETED;
                        mHandler.sendMessage(message);
                    }
                });

                threadToLoadAllStationsFromDB.start();
            }
        }


    }


    private void downloadData()
    {

        VolleyRequest.getInstance(getApplicationContext())
                .downloadStations(new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonRoot = new JSONArray(response);
                            for (int i = 0; i < jsonRoot.length(); i++) {

                                JSONObject item = jsonRoot.getJSONObject(i);

                                String id = item.optString("ID");

                                JSONObject addressInfo = item.optJSONObject("AddressInfo");

                                String title = "", address = "", town = "", stateOrProvince = "", url = "";
                                LatLng position = null;

                                if (addressInfo != null){

                                    title = addressInfo.optString("Title", "No Name");

                                    address = addressInfo.optString("AddressLine1", "No Address");

                                    town = addressInfo.optString("Town", "No Town");

                                    stateOrProvince = addressInfo.optString("StateOrProvince", "No State or Province");

                                    position = new LatLng(addressInfo.optDouble("Latitude"), addressInfo.optDouble("Longitude"));

                                    url = addressInfo.optString("RelatedURL", "No Url");
                                }

                                JSONArray mediaArray = item.optJSONArray("MediaItems");

                                String mediaUrl = null;

                                if(mediaArray != null){
                                    int k=0;
                                    while (mediaUrl == null || k != mediaArray.length()){

                                        //big image
                                        //mediaUrl = mediaArray.getJSONObject(k).optString("ItemURL", null);

                                        //small image
                                        mediaUrl = mediaArray.getJSONObject(k).optString("ItemThumbnailURL", null);
                                        k++;
                                    }
                                }

                                JSONArray connections = item.optJSONArray("Connections");

                                int numberOfPointsOfCharge = (connections != null ? connections.length() : 0 );

                                Station station = new Station(id, title, address, town, stateOrProvince, position, url, numberOfPointsOfCharge, mediaUrl);

                                for (int j = 0; j < numberOfPointsOfCharge; j++)
                                {
                                    JSONObject connection = connections.getJSONObject(j);
                                    PointOfCharge pointOfCharge = new PointOfCharge(
                                            connection.optInt("ID"), id, connection.optInt("Voltage"),
                                            connection.optInt("PowerKW"), connection.optInt("StatusTypeID")
                                    );
                                    station.addPointOfCharge(pointOfCharge);

                                }

                                stations.add(station);
                            }

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    saveData();
                                    Message message = new Message();
                                    message.what = ALL_STATIONS_SAVED;
                                    mHandler.sendMessage(message);
                                }
                            }).start();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Refresh list because the adapter data are changed
                        if(adapter != null) adapter.notifyDataSetChanged();
                    }
                }, currentPos);
    }

    private void saveData(){
        // per ogni stazione salva la stazione e tutti i suoi punti di ricarica
        for (int k = 0; k < stations.size(); k++)
        {
            Station stationToSave = stations.get(k);
            appDB.getStationDao().save(stationToSave); // salvo la stazione
            for (int p = 0; p < stationToSave.getPointsOfCharge().size(); p++)
            {
                PointOfCharge pointOfChargeToSave = stationToSave.getPointsOfCharge().get(p);
                appDB.getPointOfChargeDao().save(pointOfChargeToSave); // salvo il punto di ricarica
            }
        }
    }




}
