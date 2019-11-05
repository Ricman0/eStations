package it.univaq.estations.utility;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class LocationService {

    private static LocationService instance = null;
    public static boolean LOCATION_CHANGED; // Used to remember if the location is changed

    //location registered during last server request
    private LatLng previousLocation;
    private LatLng currentLocation;

    private LocationService() {
        this.currentLocation = null;
        this.previousLocation = null;
        this.LOCATION_CHANGED = true;
    }

    public static LocationService getInstance() {
        if(instance == null)
            synchronized(LocationService.class) {
                    instance = new LocationService();
            }
        return instance;
    }

    public LatLng getPreviousLocation() {
        return previousLocation;
    }

    public void setPreviousLocation(LatLng previousLocation) {
        this.previousLocation = previousLocation;
    }

//    Set LOCATION_UPDATED false in Settings if currentLocation is less than targetDistanceInMeter away from previousLocation
    public void evaluateDistance(Context context, int targetDistanceInMeter)
    {
        if (currentLocation == null || previousLocation == null) {
            this.LOCATION_CHANGED = true;
            return;
        }

        float[] dist = new float[1];
        android.location.Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                previousLocation.latitude, previousLocation.longitude, dist);

        if (dist[0] < targetDistanceInMeter) {
            this.LOCATION_CHANGED = false;

        } else {
            this.LOCATION_CHANGED = true;
            this.previousLocation = currentLocation;
        }

    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }
}
