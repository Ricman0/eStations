package it.univaq.estations.activity;

import android.content.Context;
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
import java.util.List;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.LocationService;
import it.univaq.estations.utility.VolleyRequest;
import it.univaq.estations.utility.PermissionService;

public class StationsList extends AppCompatActivity {

    private List<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;
    //per la posizione
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentPos;
    private Database appDB;
    Handler mHandler;
    Thread threadToLoadAllStationsFromDB;
    private static final int ALL_STATIONS_LOADED = 101;
    private static final int ALL_STATIONS_SAVED = 102;
    private static final int ALL_STATIONS_DELETED = 103;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stations_list);
        PermissionService.getInstance().permissionsCheck(this, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("E-stations");
        getSupportActionBar().setSubtitle("Stations List");

        recyclerView = findViewById(R.id.stations_list);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new StationsListAdapter(this, stations, recyclerView);
        recyclerView.setAdapter(adapter);

        currentPos = null;
        // client per fare la richiesta per ottenere la locazione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        context = this.getApplicationContext();
        appDB = Database.getInstance(getApplicationContext());
        stations = new ArrayList<>();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == ALL_STATIONS_LOADED || msg.what == ALL_STATIONS_SAVED) {
                    adapter.add(stations);
                }
                if (msg.what == ALL_STATIONS_DELETED) {
                        downloadData();
                }
            }
        };

    }

    protected void onResume() {
        super.onResume();
        //PermissionService.getInstance().permissionsCheck(this, this);

            LocationService.getInstance().evaluateDistance(context,4000);
            if (LocationService.LOCATION_CHANGED == true || LocationService.getInstance().getCurrentLocation() == null) {

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    currentPos = new LatLng(location.getLatitude(), location.getLongitude());
                                    if(LocationService.getInstance().getPreviousLocation() == null)
                                    {
                                        // inizialmente pongo previous e current position alla stessa posizione
                                        LocationService.getInstance().setPreviousLocation(currentPos);
                                    }
                                    LocationService.getInstance().setCurrentLocation(currentPos);

                                    clearDataFromDB(); // Avvio il downloadData() solo dopo che il database è stato avviato
                                    LocationService.LOCATION_CHANGED = false;
                                }
                            }
                        });
            }
            else {

                threadToLoadAllStationsFromDB = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stations.clear();
                        //get stations and all pointOFCharges from database
                        stations.addAll(appDB.getStationDao().getAllStations()); // get all stations without theirs pointOFCharges
                        for (int k = 0; k < stations.size(); k++) {
                            Station stationToFill = stations.get(k);
                            stationToFill.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationToFill.getId()));
                        }
                        Message message = new Message();
                        message.what = ALL_STATIONS_LOADED;
                        mHandler.sendMessage(message);
                    }
                });

                threadToLoadAllStationsFromDB.start();
            }

    }

    /**
     *
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void downloadData()
    {

        VolleyRequest.getInstance(getApplicationContext())
                .downloadStations(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            recyclerView.setAdapter(adapter);
                            JSONArray jsonRoot = new JSONArray(response);
                            for (int i = 0; i < jsonRoot.length(); i++) {

                                JSONObject item = jsonRoot.getJSONObject(i);

                                String id = item.optString("ID");

                                String usageCost = item.optString("UsageCost", " N/A");

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
                                        mediaUrl = mediaArray.getJSONObject(k).optString("ItemURL", null);

                                        //small image
                                        //mediaUrl = mediaArray.getJSONObject(k).optString("ItemThumbnailURL", null);
                                        k++;
                                    }
                                }

                                JSONArray connections = item.optJSONArray("Connections");

                                int numberOfPointsOfCharge = (connections != null ? connections.length() : 0 );

                                Station station = new Station(id, title, usageCost,address, town, stateOrProvince, position, url, numberOfPointsOfCharge, mediaUrl);

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

                            Thread threadToSaveStationsInDB = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    saveStationsDataInDB();
                                    Message message = new Message();
                                    message.what = ALL_STATIONS_SAVED;
                                    mHandler.sendMessage(message);
                                }
                            });
                            threadToSaveStationsInDB.start();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, currentPos, null, null, null);
    }

    /**
     * Function to save Stations and associated points of charge in the database.
     *
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void saveStationsDataInDB(){
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

    /**
     * Function to clear all stations and associated Points of charges from database.
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void clearDataFromDB(){

        Thread ThreadToClearDataFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                Database.getInstance(getApplicationContext())
                        .getStationDao().deleteAll(); // i points of charge associati dovrebbero essere eliminati con on cascade
                Message message = new Message();
                message.what = ALL_STATIONS_DELETED;
                mHandler.sendMessage(message);
            }
        });
        ThreadToClearDataFromDB.start();
    }


    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    /**
     * Function to restart the activity and clear the StationsListAdapter.
     *
     * @Override
     * @author Claudia Di Marco & Riccardo Mantini
     */
    protected void onRestart() {
        super.onRestart();
        adapter.clear();

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    /**
     * Function to define a custom behaviour for the actionBar arrow back.
     *
     * @Override
     * @return boolean
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public boolean onSupportNavigateUp(){
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        return true;
    }
}
