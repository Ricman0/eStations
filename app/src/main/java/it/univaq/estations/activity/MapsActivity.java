package it.univaq.estations.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.PermissionUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
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

                //add extras to intent
                //Station station = mDataset.get(getAdapterPosition());
                //intent.putExtra("stationId", station.getId());

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
            }
        });
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
        mUiSettings.setMyLocationButtonEnabled(true); //Sets the preference for whether rotate gestures should be enabled or disabled.
        // mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true); // richiede i permetti di access_fine o coarse

        LatLng laquila = new LatLng(42.367422, 13.349200);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(laquila).title("Marker in L'Aquila"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(laquila));
    }

    /**
     * Returns whether the checkbox with the given id is checked.
     */
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    /**
     * Checks if the map is ready (which depends on whether the Google Play services APK is
     * available. This should be called prior to calling any methods on GoogleMap.
     */
    private boolean checkReady() {
        if (mMap == null) {
//            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the zoom controls (+/- buttons in the bottom-right of the map for LTR
        // locale or bottom-left for RTL locale).
        mUiSettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the compass (icon in the top-left for LTR locale or top-right for RTL
        // locale that indicates the orientation of the map).
        mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
    }

    public void setMyLocationButtonEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the my location button (this DOES NOT enable/disable the my location
        // dot/chevron on the map). The my location button will never appear if the my location
        // layer is not enabled.
        // First verify that the location permission has been granted.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mUiSettings.setMyLocationButtonEnabled(true);
        } else {
            // Uncheck the box and request missing location permission.
            mMyLocationButtonCheckbox.setChecked(false);
         //   requestLocationPermission(MY_LOCATION_PERMISSION_REQUEST_CODE); // commentato quando ho coomentato tutta la parte dei permessi
        }
    }

    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the my location layer (i.e., the dot/chevron on the map). If enabled, it
        // will also cause the my location button to show (if it is enabled); if disabled, the my
        // location button will never show.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(mMyLocationLayerCheckbox.isChecked());
        } else {
            // Uncheck the box and request missing location permission.
            mMyLocationLayerCheckbox.setChecked(false);
//            PermissionUtils.requestPermission(this, LOCATION_LAYER_PERMISSION_REQUEST_CODE,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION, false); // commentato quando ho coomentato i permessi
        }
    }

    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables scroll gestures (i.e. panning the map).
        mUiSettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
        mUiSettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables tilt gestures.
        mUiSettings.setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables rotate gestures.
        mUiSettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

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
