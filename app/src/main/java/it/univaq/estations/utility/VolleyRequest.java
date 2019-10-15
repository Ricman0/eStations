package it.univaq.estations.utility;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.StringRes;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
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

        double curLat;
        double curLng;


        if (currentPosition != null) {

             curLat = currentPosition.latitude;
             curLng = currentPosition.longitude;
        }
        else {
            curLat = 42;
            curLng = 13;}
        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                        "https://api.openchargemap.io/v3/poi/?output=json" +
                        "&countrycode=IT" +
                        "&maxresults=20" +
                        "&latitude=" + curLat +
                        "&longitude=" + curLng + "&includecomments=true"+
                        "&compact=true&verbose=false",
                 listener,
                null);
        queue.add(request);
    }

    public void downloadImage(Response.Listener<Bitmap> listener, String url){

        ImageRequest imageRequest = new ImageRequest(url,listener,0,0,null, Bitmap.Config.RGB_565, null);
        queue.add(imageRequest);
    }
}
