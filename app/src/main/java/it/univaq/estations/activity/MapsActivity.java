package it.univaq.estations.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.GoogleLocationService;
import it.univaq.estations.utility.VolleyRequest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleLocationService.LocationListener {

    private Context context;
    private Activity activity;
    private Database appDB;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private GoogleLocationService locationService;

    private List<Station> stations = new ArrayList<>();
    private LatLng currentPos;
    private LatLng mDefaultLocation;
    private static final float DEFAULT_ZOOM = 12;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    Handler mHandler;
    Thread threadToLoadAllStationsFromDB;
    Thread threadToClearDataFromDB;
    Thread threadToSaveStationsInDB;

    private static final int ALL_STATIONS_SAVED = 102;
    private static final int ALL_STATIONS_DELETED = 103;

    private boolean stopAsking = false; // avoid keep asking for location permission if deny
    private boolean backPressed = false; // avoid redownload stations when back button is pressed

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);

        mDefaultLocation = new LatLng(42.4584, 14.216090); //DefaultLocation Pescara
        currentPos = mDefaultLocation;

        context = this.getApplicationContext();
        activity = this;
        appDB = Database.getInstance(getApplicationContext());
        stations = new ArrayList<>();

        ImageView iconToStationListActivity = findViewById(R.id.iconToStationListActivity);

        mHandler = new MyHandler(iconToStationListActivity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this); //Objects.requireNonNull(mapFragment).getMapAsync(this);

        int permissionFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        //add click listener to the navigationToStation button
        iconToStationListActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backPressed = true;
                // Open another Activity and pass to it the right station
                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(v.getContext(), StationsListActivity.class);

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    protected void onResume() {
        System.out.println(" onREsume");
        super.onResume();
        if (!backPressed) {
            if (mMap != null) {
                locationService = new GoogleLocationService();
                locationService.onCreate(this, this);
                if (!locationService.requestLocationUpdates(this) && !stopAsking) {
                    // asking for a permission
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
                }
            }
        } else {
            backPressed = false;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println(" onMapReady");
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true); //Enables (or disables if we pass false) the zoom controls.
        mUiSettings.setZoomGesturesEnabled(true); //Sets the preference for whether zoom gestures should be enabled or disabled.
        mUiSettings.setRotateGesturesEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.

        locationService = new GoogleLocationService();
        locationService.onCreate(this, this);
        //locationService.requestLastLocation(this);
        if (!locationService.requestLocationUpdates(this)) {
            onPermissionNotGranted();
        }

        // add listener to know if the camera movement has ended
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                System.out.println("on camera idle");
                // clear data in  the DB and then get bounding box and download data
                clearDataFromDB();
            }
        });

    }

    /**
     * Function to check if is available an Internet connection otherwise show an alert dialog.
     * If it is available an Internet connection, it downloads data
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void checkConnectionAndDownloadData() {
        System.out.println("checkConnectionAndDownloadData");
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
        if (!isConnected) {
            System.out.println("checkConnectionAndDownloadData non connesso");
            this.setTheme(R.style.AlertDialogEstationsTheme);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.titleAlertDialogConnectionFailed)
                    .setCancelable(false)
                    .setMessage(R.string.messageAlertDialogConnectionFailed)
                    .setPositiveButton(R.string.positiveButtonAlertDialogConnectionFailed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            checkConnectionAndDownloadData();
                        }
                    })
                    .setNegativeButton(R.string.negativeButtonAlertDialogConnectionFailed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            finishAffinity();
                            System.exit(0);

                        }
                    }).create();
            dialog.show();
        } else {
            downloadDataBoundedInBoundingBox();
        }
    }

    /**
     * Function to find the bounding box coordinates and then download estation bounded in the boundingBox
     * created by topLeftCorner and bottomRightCorner coordinates.
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void downloadDataBoundedInBoundingBox() {
        // get topLeftCorner and bottomRightCorner coordinates
        LatLng topLeftCorner = new LatLng(mMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude,
                mMap.getProjection().getVisibleRegion().latLngBounds.southwest.longitude);
        LatLng bottomRightCorner = new LatLng(mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude,
                mMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude);

        downloadData(topLeftCorner, bottomRightCorner);
    }


    /**
     * Function to add a marker for the estation passed as parameter on the Map.
     *
     * @param n Station eStation to add on the map
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void addEStationMarker(Station n) {
        System.out.println("addEStationMarker");
        // create and add marker to the map
        Marker estationMarker = mMap.addMarker(new MarkerOptions().position(n.getPosition()).title("E-Station : " + n.getName()));
        // Changing marker icon_green
        if (n.isFree() == true) {
            //estationMarker.setIcon(vectorToBitmap(R.drawable.icon_green));
            estationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            //estationMarker.setIcon(vectorToBitmap(R.drawable.icon_red));
            estationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        estationMarker.setTag(n.getId());

        // Lister to add custom behaviour to click on a marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                backPressed = true;
                String stationId = (String) marker.getTag();

                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(context, DetailsActivity.class);

                //add extras to intent
                intent.putExtra("stationId", stationId);

                //Avendo l’intent, per avviare la nuova activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                // check permission to add or not navigation elements
                int permissionFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
                    // don't add navigation elements but show marker title
                    marker.showInfoWindow();
                    return true;
                } else {
                    // to add navigation elements  //return false;
                    return true;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationService.stopLocationUpdates(this);
        if (threadToLoadAllStationsFromDB != null && threadToLoadAllStationsFromDB.isAlive()) {
            threadToLoadAllStationsFromDB.interrupt();
        }
        if (threadToClearDataFromDB != null && threadToClearDataFromDB.isAlive()) {
            threadToClearDataFromDB.interrupt();
        }
        if (threadToSaveStationsInDB != null && threadToSaveStationsInDB.isAlive()) {
            threadToSaveStationsInDB.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Function to clear all stations and associated Points of charges from database.
     * To do this, this function creates a new thread and when it finishes, it sends ALL_STATIONS_DELETED message
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void clearDataFromDB() {
        System.out.println(" clearDataFromDB");
        stations.clear();
        System.out.println(stations.size() + " stazioni");
        threadToClearDataFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                Database.getInstance(getApplicationContext())
                        .getStationDao().deleteAll();// i points of charge associati dovrebbero essere eliminati con on cascade
                Message message = new Message();
                message.what = ALL_STATIONS_DELETED;
                mHandler.sendMessage(message);
            }
        });
        threadToClearDataFromDB.start();
    }

    /**
     * Function to download estation bounded in the boundingBox created by topLeftCorner and bottomRightCorner.
     *
     * @param topLeftCorner     LatLng top left corner of the bounding box map
     * @param bottomRightCorner LatLng bottom right corner of the bounding box map
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void downloadData(LatLng topLeftCorner, LatLng bottomRightCorner) {
        System.out.println(" downloadData");
        VolleyRequest.getInstance(getApplicationContext())
                .downloadStations(new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            //recyclerView.setAdapter(adapter);
                            JSONArray jsonRoot = new JSONArray(response);
                            for (int i = 0; i < jsonRoot.length(); i++) {

                                JSONObject item = jsonRoot.getJSONObject(i);

                                String id = item.optString("ID");

                                String usageCost = item.optString("UsageCost", " N/A");

                                JSONObject addressInfo = item.optJSONObject("AddressInfo");

                                String title = "", address = "", town = "", stateOrProvince = "", url = "";
                                LatLng position = null;

                                if (addressInfo != null) {

                                    title = addressInfo.optString("Title", "No Name");

                                    address = addressInfo.optString("AddressLine1", "No Address");

                                    town = addressInfo.optString("Town", "No Town");

                                    stateOrProvince = addressInfo.optString("StateOrProvince", "No State or Province");

                                    position = new LatLng(addressInfo.optDouble("Latitude"), addressInfo.optDouble("Longitude"));

                                    url = addressInfo.optString("RelatedURL", "No Url");
                                }

                                JSONArray mediaArray = item.optJSONArray("MediaItems");

                                String mediaUrl = null;

                                if (mediaArray != null) {
                                    int k = 0;
                                    while (mediaUrl == null || k != mediaArray.length()) {

                                        //big image
                                        mediaUrl = mediaArray.getJSONObject(k).optString("ItemURL", null);

                                        //small image
                                        //mediaUrl = mediaArray.getJSONObject(k).optString("ItemThumbnailURL", null);
                                        k++;
                                    }
                                }

                                JSONArray connections = item.optJSONArray("Connections");

                                int numberOfPointsOfCharge = (connections != null ? connections.length() : 0);

                                Station station = new Station(id, title, usageCost, address, town, stateOrProvince, position, url, numberOfPointsOfCharge, mediaUrl);

                                station.calcAndSetDistanceFromUser(currentPos);
                                for (int j = 0; j < numberOfPointsOfCharge; j++) {
                                    JSONObject connection = connections.getJSONObject(j);
                                    PointOfCharge pointOfCharge = new PointOfCharge(
                                            connection.optInt("ID"), id, connection.optInt("Voltage"),
                                            connection.optInt("PowerKW"), connection.optInt("StatusTypeID")
                                    );
                                    station.addPointOfCharge(pointOfCharge);
                                }
                                stations.add(station);

                            }

                            threadToSaveStationsInDB = new Thread(new Runnable() {
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
                }, currentPos, null, topLeftCorner, bottomRightCorner);
    }

    /**
     * Function to save Stations and associated points of charge in the database.
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void saveStationsDataInDB() {
        System.out.println("saveStationsDataInDB");
        // per ogni stazione salva la stazione e tutti i suoi punti di ricarica
        for (int k = 0; k < stations.size(); k++) {
            Station stationToSave = stations.get(k);
            appDB.getStationDao().save(stationToSave); // salvo la stazione
            for (int p = 0; p < stationToSave.getPointsOfCharge().size(); p++) {
                PointOfCharge pointOfChargeToSave = stationToSave.getPointsOfCharge().get(p);
                appDB.getPointOfChargeDao().save(pointOfChargeToSave); // salvo il punto di ricarica
            }
        }
    }

    @Override
    public void onLastLocationResult(Location location) {
        System.out.println("onLastLocationResult");
        currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
        mUiSettings.setMyLocationButtonEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentPos)      // Sets the center of the map to Mountain View
                .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    @Override
    public void onLastLocationNullResult() {
        System.out.println("onLastLocationNullResult");
        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
        mUiSettings.setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
    }

    @Override
    public void onPermissionNotGranted() {
        System.out.println("onPermissionNotGranted");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //allow  // on resume viene richiamato non appena il popup sparisce
                } else {
                    // deny
                    stopAsking = true;
                    currentPos = mDefaultLocation;
                }
                break;

            default:
                break;

        }


    }


    private class MyHandler extends Handler {

        private final ImageView iconToStationListActivity;

        public MyHandler(ImageView iconToStationListActivity) {
            this.iconToStationListActivity = iconToStationListActivity;
        }

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (msg.what == ALL_STATIONS_SAVED) {

                iconToStationListActivity.setEnabled(true);
                iconToStationListActivity.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_white));
                System.out.println("stazion.size in ALL_STATIONS_SAVED " + stations.size());
                //add a market for each station
                for (int y = 0; y < stations.size(); y++) {
                    addEStationMarker(stations.get(y));
                }
            }
            if (msg.what == ALL_STATIONS_DELETED) {
                stations.clear();
                if (mMap != null) {
                    mMap.clear();
                }
                iconToStationListActivity.setEnabled(false);
                iconToStationListActivity.setBackgroundColor(ContextCompat.getColor(context, R.color.disabled_color));
                checkConnectionAndDownloadData();
            }
        }
    }
}


