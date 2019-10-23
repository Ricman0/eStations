package it.univaq.estations.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

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

    public void downloadStations(Response.Listener<String> listener, LatLng currentPosition, Integer kmDistance,
                                 LatLng topLeftCorner, LatLng bottomRightCorner){

        double curLat;
        double curLng;

        if (currentPosition != null) {

             curLat = currentPosition.latitude;
             curLng = currentPosition.longitude;
        }
        else {
            curLat = 42;
            curLng = 13;}

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.openchargemap.io")
                .appendPath("v3")
                .appendPath("poi")
                .appendQueryParameter("output", "json")
                .appendQueryParameter("countrycode", "IT")
                .appendQueryParameter("latitude", String.valueOf(curLat))
                .appendQueryParameter("longitude", String.valueOf(curLng))
                .appendQueryParameter("includecomments", "true")
                .appendQueryParameter("compact", "true")
                .appendQueryParameter("verbose", "false");

        if (kmDistance != null){
            builder.appendQueryParameter("distance", String.valueOf(kmDistance))
                    .appendQueryParameter("distanceunit", "KM");
        }

        if (topLeftCorner != null && bottomRightCorner !=null){
            builder.appendQueryParameter("boundingbox", "(" + topLeftCorner.latitude +","+ topLeftCorner.longitude +")," +
                    "("+bottomRightCorner.latitude + "," + bottomRightCorner.longitude + ")");
        }
        String myUrl = builder.build().toString();

        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                myUrl,
                listener,
                null);
        queue.add(request);
    }

    public void downloadImage(Response.Listener<Bitmap> listener, String url){

        ImageRequest imageRequest = new ImageRequest(url,listener,0,0,null, Bitmap.Config.RGB_565, null);
        queue.add(imageRequest);
    }
}
