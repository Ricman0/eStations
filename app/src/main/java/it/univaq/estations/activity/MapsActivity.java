package it.univaq.estations.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.LocationService;
import it.univaq.estations.utility.PermissionService;
import it.univaq.estations.utility.VolleyRequest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
//public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<Station> stations = new ArrayList<>();

    private UiSettings mUiSettings;

    private CheckBox mMyLocationButtonCheckbox;

    private CheckBox mMyLocationLayerCheckbox;

    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;

    private boolean mLocationPermissionDenied = false;

    private FusedLocationProviderClient mfusedLocationClient;
    private LatLng currentPos;
    private Database appDB;
    Handler mHandler;
    Thread threadToLoadAllStationsFromDB;
    private static final int ALL_STATIONS_LOADED = 101;
    private static final int ALL_STATIONS_SAVED = 102;
    private static final int ALL_STATIONS_DELETED = 103;
    private LatLng  mDefaultLocation;
    private  static final float DEFAULT_ZOOM = 12;
    private Context context;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        context = this.getApplicationContext();
        activity = this;
        mDefaultLocation = new LatLng(42.367422, 13.349200);
        currentPos = null;
        // client per fare la richiesta per ottenere la locazione
        mfusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        appDB = Database.getInstance(getApplicationContext());
        stations = new ArrayList<>();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                final int kmDistance = 0;

                super.handleMessage(msg);
                if (msg.what == ALL_STATIONS_LOADED || msg.what == ALL_STATIONS_SAVED) {
                    // per ogni stazione aggiungi un marker
                    for (int y = 0; y < stations.size(); y++)
                    {
                        addEStationMarker(stations.get(y));
                    }

                }
                if (msg.what == ALL_STATIONS_DELETED) {
                    downloadData(kmDistance);
                    for (int y = 0; y < stations.size(); y++)
                    {
                        addEStationMarker(stations.get(y));
                    }
                }
            }
        };
        //this.setContentView(R.layout.ui_setting_demo);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //Objects.requireNonNull(mapFragment).getMapAsync(this);

        //add click listener to the navigationToStation button
        ImageView icon = findViewById(R.id.iconToStationListActivity);
        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Open another Activity and pass to it the right station
                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(v.getContext(), StationsList.class);

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Function to add a marker for the estation passed as parameter on the Map.
     *
     * @param n Station eStation to add on the map
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void addEStationMarker(Station n) {

        // create marker
        MarkerOptions marker = new MarkerOptions().position(n.getPosition()).title("E-Station : "+ n.getName());

        Bitmap battery_charging_icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_battery_charging_90_black_24dp);

// Changing marker icon
        //marker.icon(BitmapDescriptorFactory.fromBitmap(battery_charging_icon));
        //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_battery_charging_90_black_18dp));
marker.icon(bitmapDescriptorFromVector(this, R.drawable.ic_battery_charging_90_black_24dp));
// adding marker
        mMap.addMarker(marker);

        //mMap.addMarker(new MarkerOptions().position(n.getPosition()).title("E-Station : "+ n.getName()));
                //.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_battery_charging_90_black_18dp));

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_battery_charging_90_black_24dp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
       // LocationService.LOCATION_CHANGED = true;

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

        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        //mUiSettings.setCompassEnabled(true); //Enables (or disables) the compass.
        mUiSettings.setZoomControlsEnabled(true); //Enables (or disables if we pass false) the zoom controls.
        mUiSettings.setZoomGesturesEnabled(true); //Sets the preference for whether zoom gestures should be enabled or disabled.
        //mUiSettings.setTiltGesturesEnabled(true); //Sets the preference for whether tilt gestures should be enabled or disabled.
        mUiSettings.setRotateGesturesEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            PermissionService.getInstance().permissionsCheck(context, activity);
            if (PermissionService.getInstance().isFineLocationPermissionGranted()) {
                mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
                mUiSettings.setMyLocationButtonEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                PermissionService.getInstance().permissionsCheck(context, activity);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (PermissionService.getInstance().isFineLocationPermissionGranted()) {
                if (LocationService.LOCATION_CHANGED == true || LocationService.getInstance().getPreviousLocation() == null)
                {
                    mfusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        currentPos = new LatLng(location.getLatitude(), location.getLongitude());
                                        LocationService.getInstance().setPreviousLocation(currentPos);
                                        clearDataFromDB();
                                        LocationService.LOCATION_CHANGED = false;
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, DEFAULT_ZOOM));
                                        mMap.addMarker(new MarkerOptions().position(currentPos).title("You are here"));
                                    }
                                    else {
                                        //Log.d(TAG, "Current location is null. Using defaults.");
                                        //Log.e(TAG, "Exception: %s", task.getException());
                                        // move the camera and add a marker in my position
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
                                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                        mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("You are in L'Aquila"));

                                    }
                                }
                            });
                }
                else{
                    currentPos = LocationService.getInstance().getPreviousLocation();

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, DEFAULT_ZOOM));

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
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    protected void onResume() {
        super.onResume();
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

    private void downloadData(int kmDistance)
    {

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
                }, currentPos, kmDistance);
    }

    /**
     * Function to save Stations and associated points of charge in the database.
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

//    /**
//     * Returns whether the checkbox with the given id is checked.
//     */
//    private boolean isChecked(int id) {
//        return ((CheckBox) findViewById(id)).isChecked();
//    }
//
//    /**
//     * Checks if the map is ready (which depends on whether the Google Play services APK is
//     * available. This should be called prior to calling any methods on GoogleMap.
//     */
//    private boolean checkReady() {
//        if (mMap == null) {
//            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }
//
//    public void setZoomButtonsEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables the zoom controls (+/- buttons in the bottom-right of the map for LTR
//        // locale or bottom-left for RTL locale).
//        mUiSettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
//    }
//
//    public void setCompassEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables the compass (icon in the top-left for LTR locale or top-right for RTL
//        // locale that indicates the orientation of the map).
//        mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
//    }
//
//    public void setMyLocationButtonEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables the my location button (this DOES NOT enable/disable the my location
//        // dot/chevron on the map). The my location button will never appear if the my location
//        // layer is not enabled.
//        // First verify that the location permission has been granted.
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mUiSettings.setMyLocationButtonEnabled(true);
//        } else {
//            // Uncheck the box and request missing location permission.
//            mMyLocationButtonCheckbox.setChecked(false);
//         //   requestLocationPermission(MY_LOCATION_PERMISSION_REQUEST_CODE); // commentato quando ho coomentato tutta la parte dei permessi
//        }
//    }
//
//    public void setMyLocationLayerEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables the my location layer (i.e., the dot/chevron on the map). If enabled, it
//        // will also cause the my location button to show (if it is enabled); if disabled, the my
//        // location button will never show.
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(mMyLocationLayerCheckbox.isChecked());
//        } else {
//            // Uncheck the box and request missing location permission.
//            mMyLocationLayerCheckbox.setChecked(false);
////            PermissionUtils.requestPermission(this, LOCATION_LAYER_PERMISSION_REQUEST_CODE,
////                    android.Manifest.permission.ACCESS_FINE_LOCATION, false); // commentato quando ho coomentato i permessi
//        }
//    }
//
//    public void setScrollGesturesEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables scroll gestures (i.e. panning the map).
//        mUiSettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
//    }
//
//    public void setZoomGesturesEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
//        mUiSettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
//    }
//
//    public void setTiltGesturesEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables tilt gestures.
//        mUiSettings.setTiltGesturesEnabled(((CheckBox) v).isChecked());
//    }
//
//    public void setRotateGesturesEnabled(View v) {
//        if (!checkReady()) {
//            return;
//        }
//        // Enables/disables rotate gestures.
//        mUiSettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
//    }

//    /**
//     * Requests the fine location permission. If a rationale with an additional explanation should
//     * be shown to the user, displays a dialog that triggers the request.
//     */
//    public void requestLocationPermission(int requestCode) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)) {
//            // Display a dialog with rationale.
//            PermissionUtils.RationaleDialog
//                    .newInstance(requestCode, false).show(
//                    getSupportFragmentManager(), "dialog");
//        } else {
//            // Location permission has not been granted yet, request it.
//            PermissionUtils.requestPermission(this, requestCode,
//                    Manifest.permission.ACCESS_FINE_LOCATION, false);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
//            // Enable the My Location button if the permission has been granted.
//            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                mUiSettings.setMyLocationButtonEnabled(true);
//                mMyLocationButtonCheckbox.setChecked(true);
//            } else {
//                mLocationPermissionDenied = true;
//            }
//
//        } else if (requestCode == LOCATION_LAYER_PERMISSION_REQUEST_CODE) {
//            // Enable the My Location layer if the permission has been granted.
//            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                mMap.setMyLocationEnabled(true);
//                mMyLocationLayerCheckbox.setChecked(true);
//            } else {
//                mLocationPermissionDenied = true;
//            }
//        }
//    }
//
//    @Override
//    protected void onResumeFragments() {
//        super.onResumeFragments();
//        if (mLocationPermissionDenied) {
//            PermissionUtils.PermissionDeniedDialog
//                    .newInstance(false).show(getSupportFragmentManager(), "dialog");
//            mLocationPermissionDenied = false;
//        }
//    }
}


