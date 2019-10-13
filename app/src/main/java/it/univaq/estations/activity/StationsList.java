package it.univaq.estations.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

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

public class StationsList extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 2;
    private ArrayList<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;
    //per la posizione
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentPos;
    private Database appDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_stations_list);

        recyclerView = findViewById(R.id.stations_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new StationsListAdapter(this, stations);
        recyclerView.setAdapter(adapter);
        appDB = Database.getInstance(getApplicationContext());

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
//        this.permissionCheck();

        // se fusedLocationClient.getLastLocation() == l'ultima posizione memorizzata allora recupero dal db altimenti richiedo; cancello e memorizzo nuove stazioni.
        boolean location_updated = Settings.loadBoolean(getApplicationContext(), Settings.LOCATION_UPDATED, true);
        if(location_updated == true){

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null)
                            {
                                //delete all database table
                                appDB.clearAllTables(); // è asincrono

                                currentPos = new LatLng( location.getLatitude(), location.getLongitude());

                                // Registering the receiver
                                //                    LocalBroadcastManager.getInstance(getApplicationContext())
                                //                            .registerReceiver(myReceiver, new IntentFilter(RequestService.FILTER_REQUEST_DOWNLOAD));
                                downloadData();
                                saveData();
                                Settings.save(getApplicationContext(), Settings.LOCATION_UPDATED, false);
                            }
                        }
                    });
        }
        else
            {
                //get stations and all pointOFCharges from database
                stations.addAll( appDB.getStationDao().getAllStations()); // get all stations without theirs pointOFCharges
                for (int k = 0; k < stations.size(); k++)
                {
                    Station stationToFill = stations.get(k);
                    stationToFill.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointOfCharges(stationToFill.getId()));
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

                                JSONArray connections = item.optJSONArray("Connections");

                                int numberOfPointsOfCharge = (connections != null ? connections.length() : 0 );

                                Station station = new Station(id, title, address, town, stateOrProvince, position, url, numberOfPointsOfCharge);

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
            for (int p = 0; p < stationToSave.getPointOfCharges().size(); p++)
            {
                PointOfCharge pointOfChargeToSave = stationToSave.getPointOfCharges().get(p);
                appDB.getPointOfChargeDao().save(pointOfChargeToSave); // salvo il punto di ricarica
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_INTERNET: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:{
//
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request.
//            break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + requestCode);
//        }
//    }

//    private void permissionCheck(){
//
//            boolean permissionAccessCoarseLocationApproved =
//                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED;
//            boolean permissionAccessFineLocationApproved =
//                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED;
//            boolean permissionAccessInternetApproved =
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
//                    == PackageManager.PERMISSION_GRANTED;
//
//            if (!permissionAccessCoarseLocationApproved) {
//                // Permission is not granted
////                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
////                        Manifest.permission.READ_CONTACTS)) {
////                    // Show an explanation to the user *asynchronously* -- don't block
////                    // this thread waiting for the user's response! After the user
////                    // sees the explanation, try again to request the permission.
////                } else {
//                    // No explanation needed; request the permission
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                            MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
//
//                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                    // app-defined int constant. The callback method gets the
//                    // result of the request.
//                //}
//            } else {
//                // Permission has already been granted        }
//
//        }
//    }

    }
