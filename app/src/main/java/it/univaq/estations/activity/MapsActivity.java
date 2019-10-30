package it.univaq.estations.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

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
import it.univaq.estations.utility.LocationService;
import it.univaq.estations.utility.GoogleLocationService;
import it.univaq.estations.utility.PermissionService;
import it.univaq.estations.utility.VolleyRequest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleLocationService.LocationListener {

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1 ;
    private GoogleMap mMap;
    private UiSettings mUiSettings;

    private List<Station> stations = new ArrayList<>();

    //private MyListener listener = new MyListener(); se serve decommentare

    private GoogleLocationService locationService;

    //private FusedLocationProviderClient mfusedLocationClient; il client è nella classe di utility

    private LatLng currentPos;
    private LatLng  mDefaultLocation;

    private Context context;
    private Activity activity;
    private Database appDB;

    Handler mHandler;
    Thread threadToLoadAllStationsFromDB;
    private static final int ALL_STATIONS_LOADED = 101;
    private static final int ALL_STATIONS_SAVED = 102;
    private static final int ALL_STATIONS_DELETED = 103;

    private  static final float DEFAULT_ZOOM = 12;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // do what i need
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);

        mDefaultLocation = new LatLng( 42.4584, 14.216090);
        currentPos = null;

        context = this.getApplicationContext();
        activity = this;
        appDB = Database.getInstance(getApplicationContext());
        stations = new ArrayList<>();

        //todo: da cancellare se tutto andrà per il meglio
//        // client per fare la richiesta per ottenere la locazione
//        mfusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // todo: mHandler da cancellare se si può usare il receveir
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                if (msg.what == ALL_STATIONS_LOADED || msg.what == ALL_STATIONS_SAVED) {
                    // per ogni stazione aggiungi un marker
                    for (int y = 0; y < stations.size(); y++)
                    {
                        addEStationMarker(stations.get(y));
                    }

                }
                if (msg.what == ALL_STATIONS_DELETED) {

                    downloadDataBoundedInBoundingBox();
                    for (int y = 0; y < stations.size(); y++)
                    {
                        addEStationMarker(stations.get(y));
                    }
                }
            }
        };


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null ) mapFragment.getMapAsync(this); //Objects.requireNonNull(mapFragment).getMapAsync(this);

        //add click listener to the navigationToStation button
        ImageView icon = findViewById(R.id.iconToStationListActivity);
        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Open another Activity and pass to it the right station
                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(v.getContext(), StationsList.class);

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

    }

    /**
     * Function to find the bounding box coordinates and then download estation bounded in the boundingBox
     * created by topLeftCorner and bottomRightCorner coordinates.
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void downloadDataBoundedInBoundingBox()
    {
        // get topLeftCorner and bottomRightCorner coordinates
        LatLng topLeftCorner = new LatLng(mMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude,
                mMap.getProjection().getVisibleRegion().latLngBounds.southwest.longitude );
        LatLng bottomRightCorner = new LatLng(mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude,
                mMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude);

        // downloadData
        downloadData(topLeftCorner, bottomRightCorner);
    }


    /**
     * Function to add a marker for the estation passed as parameter on the Map.
     *
     * @param n Station eStation to add on the map
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void addEStationMarker(Station n) {
        // create and add marker to the map
        Marker estationMarker =  mMap.addMarker(new MarkerOptions().position(n.getPosition()).title("E-Station : " + n.getName()));
        // Changing marker icon_green
        if(n.isFree() == true) {
            //estationMarker.setIcon(vectorToBitmap(R.drawable.icon_green));
            estationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        else{
            //estationMarker.setIcon(vectorToBitmap(R.drawable.icon_red));
            estationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        estationMarker.setTag(n.getId());

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                String stationId = (String) marker.getTag();

                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(context, DetailsActivity.class);

                //add extras to intent
                intent.putExtra("stationId", stationId);

                //Avendo l’intent, per avviare la nuova activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                return false;
            }
        });
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

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon_green.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap((vectorDrawable.getIntrinsicWidth()),
                (vectorDrawable.getIntrinsicHeight()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        //DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
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
        mUiSettings.setZoomControlsEnabled(true); //Enables (or disables if we pass false) the zoom controls.
        mUiSettings.setZoomGesturesEnabled(true); //Sets the preference for whether zoom gestures should be enabled or disabled.
        mUiSettings.setRotateGesturesEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.

        // todo: forse da eliminare
//        // Turn on the My Location layer and the related control on the map.
//        updateLocationUI();
//        // Get the current location of the device and set the position of the map.
//        getDeviceLocation();

        // add listener to know if the camera movement has ended
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                    // get bounding box and download data
                    downloadDataBoundedInBoundingBox();
            }
        });
        locationService = new GoogleLocationService();
        locationService.onCreate(this, this);
        locationService.requestLastLocation(this);
    }




//    private void updateLocationUI() {
//        if (mMap == null) {
//            return;
//        }
//        try {
//            PermissionService.getInstance().permissionsCheck(context, activity);
//            if (PermissionService.getInstance().isFineLocationPermissionGranted()) {
//                mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
//                mUiSettings.setMyLocationButtonEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.
//            } else {
//                mMap.setMyLocationEnabled(false);
//                mMap.getUiSettings().setMyLocationButtonEnabled(false);
//                PermissionService.getInstance().permissionsCheck(context, activity);
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }

//    private void getDeviceLocation() {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        try {
//            PermissionService.getInstance().permissionsCheck(context, activity);
//            if (PermissionService.getInstance().isFineLocationPermissionGranted()) {
//                if (LocationService.LOCATION_CHANGED == true || LocationService.getInstance().getCurrentLocation() == null)
//                {
//                    mfusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    if (location != null) {
//                                        currentPos = new LatLng(location.getLatitude(), location.getLongitude());
//                                        if(LocationService.getInstance().getPreviousLocation() == null)
//                                        {
//                                            // inizialmente pongo previous e current position alla stessa posizione
//                                            LocationService.getInstance().setPreviousLocation(currentPos);
//                                        }
//                                        LocationService.getInstance().setCurrentLocation(currentPos);
//                                        clearDataFromDB();
//                                        LocationService.LOCATION_CHANGED = false;
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, DEFAULT_ZOOM));
//                                    }
//                                    else {
//                                        // move the camera and add a marker in my position
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
//                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
//                                       // mMap.getUiSettings().setMyLocationButtonEnabled(false);
//
//                                    }
//                                }
//                            });
//                }
//                else{
//                    currentPos = LocationService.getInstance().getCurrentLocation();
//
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, DEFAULT_ZOOM));
//
//                    threadToLoadAllStationsFromDB = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            stations.clear();
//                            //get stations and all pointOFCharges from database
//                            stations.addAll(appDB.getStationDao().getAllStations()); // get all stations without theirs pointOFCharges
//                            for (int k = 0; k < stations.size(); k++) {
//                                Station stationToFill = stations.get(k);
//                                stationToFill.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationToFill.getId()));
//                            }
//                            Message message = new Message();
//                            message.what = ALL_STATIONS_LOADED;
//                            mHandler.sendMessage(message);
//                        }
//                    });
//
//                    threadToLoadAllStationsFromDB.start();
//                }
//
//            }
//            else
//            {
//                currentPos = mDefaultLocation;
//                if(LocationService.getInstance().getPreviousLocation() == null)
//                {
//                    // inizialmente pongo previous e current position alla stessa posizione
//                    LocationService.getInstance().setPreviousLocation(currentPos);
//                }
//                LocationService.getInstance().setCurrentLocation(currentPos);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//            }
//        } catch(SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }

    protected void onResume() {
        super.onResume();
        if(mMap != null) {
            locationService = new GoogleLocationService();
            locationService.onCreate(this, this);
            locationService.requestLastLocation(this);
            //todo: da eliminare ??
//        registerReceiver(mReceiver, new IntentFilter(PermissionService.PERMISSION_GRANTED));
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

    /**
     * Function to download estation bounded in the boundingBox created by topLeftCorner and bottomRightCorner.
     *
     * @param topLeftCorner LatLng top left corner of the bounding box map
     * @param bottomRightCorner LatLng bottom right corner of the bounding box map
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void downloadData(LatLng topLeftCorner, LatLng bottomRightCorner)
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
                }, currentPos, null, topLeftCorner, bottomRightCorner);
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

    @Override
    public void onLastLocationResult(Location location) {
        currentPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(LocationService.getInstance().getPreviousLocation() == null)
        {
            // inizialmente pongo previous e current position alla stessa posizione
            LocationService.getInstance().setPreviousLocation(currentPos);
        }
        LocationService.getInstance().setCurrentLocation(currentPos);
        clearDataFromDB();
        LocationService.LOCATION_CHANGED = false;
        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
        mUiSettings.setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, DEFAULT_ZOOM));
    }

    @Override
    public void onLastLocationNullResult() {
        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
        mUiSettings.setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
    }

    @Override
    public void onPermissionNotGranted() {
        currentPos = mDefaultLocation;
        if(LocationService.getInstance().getPreviousLocation() == null)
        {
            // inizialmente pongo previous e current position alla stessa posizione
            LocationService.getInstance().setPreviousLocation(currentPos);
        }
        LocationService.getInstance().setCurrentLocation(currentPos);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    public void onLoadAllStationFromDB() {
        currentPos = LocationService.getInstance().getCurrentLocation();
        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse
        mUiSettings.setMyLocationButtonEnabled(true);
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //allow
                    // on resume viene richiamato non appena il popup sparisce

                }
                else{
                    // deny
                }
                break;

                default:break;

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
//        // Enables/disables the compass (icon_green in the top-left for LTR locale or top-right for RTL
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


