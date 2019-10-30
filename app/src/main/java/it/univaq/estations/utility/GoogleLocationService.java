package it.univaq.estations.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class GoogleLocationService {

    private FusedLocationProviderClient providerClient;

    private GoogleLocationService.LocationListener listener;

//    private LocationCallback locationCallback = new LocationCallback() {
//
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            super.onLocationResult(locationResult);
//            if(listener != null) listener.onLocationChanged(locationResult.getLastLocation());
//        }
//
//        @Override
//        public void onLocationAvailability(LocationAvailability locationAvailability) {
//            super.onLocationAvailability(locationAvailability);
//            System.out.println("Is location available? " + locationAvailability.isLocationAvailable());
//        }
//    };

    public void onCreate(Activity activity, GoogleLocationService.LocationListener listener){
        // client per fare la richiesta per ottenere la locazione
        providerClient = LocationServices.getFusedLocationProviderClient(activity);
        this.listener = listener;
    }

    /**
     * Check if the Google Play Services are available.
     *
     * @param context of your application
     * @return true if they are available or false otherwise
     */
    public boolean areGoogleServicesAvailable(Context context){

        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }



    /**
     * Get last known location
     *
     * @param activity the instance of the Activity
     * @return true if the permissions are granted, false otherwise
     */
    public boolean requestLastLocation(Activity activity){

        int permissionFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionFineLocation == PackageManager.PERMISSION_GRANTED) {
            if (LocationService.LOCATION_CHANGED == true || LocationService.getInstance().getCurrentLocation() == null)
            {
                if(areGoogleServicesAvailable(activity)) {
                    if (LocationService.LOCATION_CHANGED == true || LocationService.getInstance().getCurrentLocation() == null) {

                        providerClient.getLastLocation()
                                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (listener != null) {
                                            if (location != null)
                                                listener.onLastLocationResult(location);
                                            else
                                                listener.onLastLocationNullResult();
                                        }

                                    }
                                });
                    }
                    else{
                        listener.onLoadAllStationFromDB();
                    }
                }
                }
                return true;
        }
        else{
            listener.onPermissionNotGranted();
            return false;
        }
    }


    public interface LocationListener {

        void onLastLocationResult(Location location);
        void onLastLocationNullResult();
        void onPermissionNotGranted();
        void onLoadAllStationFromDB();
    }
}
