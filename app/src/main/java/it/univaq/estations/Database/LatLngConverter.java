package it.univaq.estations.Database;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class to convert Latlng object
 */
public class LatLngConverter {

        @TypeConverter
        public static String toString(LatLng position) {
            return position.toString();
        }

        @TypeConverter
        public static LatLng toLatLng(String stringPosition) {
            String[] latlong =  stringPosition.split(",");
            latlong[0]=latlong[0].replace("lat/lng: (","");
            latlong[1]=latlong[1].replace(")","");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            return new LatLng(latitude, longitude);
        }

}
