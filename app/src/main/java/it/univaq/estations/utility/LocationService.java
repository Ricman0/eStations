package it.univaq.estations.utility;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class LocationService {

    private static LocationService instance = null;

    //location registered during last server request
    private LatLng previousLocation;

    private LocationService() {
        this.previousLocation = null;
    }

    public static LocationService getInstance() {
        if(instance == null)
            synchronized(LocationService.class) {
                if( instance == null )
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
    public void evaluateDistance(Context context, LatLng currentLocation, int targetDistanceInMeter)
    {
        if (currentLocation == null) {
            Settings.save(context, Settings.LOCATION_CHANGED, true);
            return;
        }

        float[] dist = new float[1];
        android.location.Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                previousLocation.latitude, previousLocation.longitude, dist);

        if (dist[0] < targetDistanceInMeter) {
            Settings.save(context, Settings.LOCATION_CHANGED, false);
        } else {
            Settings.save(context, Settings.LOCATION_CHANGED, true);
            this.previousLocation = currentLocation;
        }

    }
}
