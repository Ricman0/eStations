package it.univaq.estations.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.RequestService;
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
//        this.permissionCheck();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    currentPos = new LatLng( location.getLatitude(), location.getLongitude());
                }
            }
        });

//        if (requestingLocationUpdates) {
//            startLocationUpdates();
//        }
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


//    private void startLocationUpdates() {
//        fusedLocationClient.requestLocationUpdates(locationRequest,
//                locationCallback,
//                Looper.getMainLooper());
//    }

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

                                String id = item.getString("ID");

                                JSONObject addressInfo = item.getJSONObject("AddressInfo");

                                String title = addressInfo.getString("Title");

                                String address = addressInfo.getString("AddressLine1");

                                String town = addressInfo.getString("Town");

                                String stateOrProvince = addressInfo.getString("StateOrProvince");

                                LatLng position = new LatLng(addressInfo.getDouble("Latitude"), addressInfo.getDouble("Longitude"));

                                String url = addressInfo.getString("RelatedURL");

                                JSONArray connections = item.getJSONArray("Connections");

                                int numberOfConnections = connections.length();


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
                }, currentPos);
    }

        private void permissionCheck(){

            boolean permissionAccessCoarseLocationApproved =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
            boolean permissionAccessFineLocationApproved =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
            boolean permissionAccessInternetApproved =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED;

            if (!permissionAccessCoarseLocationApproved) {
                // Permission is not granted
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                        Manifest.permission.READ_CONTACTS)) {
//                    // Show an explanation to the user *asynchronously* -- don't block
//                    // this thread waiting for the user's response! After the user
//                    // sees the explanation, try again to request the permission.
//                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                //}
            } else {
                // Permission has already been granted        }

        }
    }

   // @Override
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
}
