package it.univaq.estations.utility;

import android.content.Context;

import androidx.annotation.StringRes;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import it.univaq.estations.R;

public class VolleyRequest {

    private RequestQueue queue;

    private static VolleyRequest instance = null;

    public static VolleyRequest getInstance(Context context){
        return instance == null ? instance = new VolleyRequest(context) : instance;
    }

    private VolleyRequest(Context context){

        queue = Volley.newRequestQueue(context);
    }

    public void downloadStations(Response.Listener<String> listener, LatLng currentPosition){

        double curLat = currentPosition.latitude;
        double curLng = currentPosition.longitude;
        StringRequest request = new StringRequest(
                StringRequest.Method.GET,

                R.string.baseStationRequestUrl +
                        "?output=json" +
                        "&countrycode=IT" +
                        "&maxresults=4" +
                        "&latitude=" + curLat +
                        "&longitude=" + curLng +
                        "&compact=true&verbose=false",
                 listener,
                null);
        queue.add(request);
    }
}
