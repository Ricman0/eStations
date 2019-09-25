package it.univaq.estations.utility;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class VolleyRequest {

    private RequestQueue queue;

    private static VolleyRequest instance = null;

    public static VolleyRequest getInstance(Context context){
        return instance == null ? instance = new VolleyRequest(context) : instance;
    }

    private VolleyRequest(Context context){

        queue = Volley.newRequestQueue(context);
    }

    public void downloadStations(Response.Listener<String> listener){

        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                "https://api.openchargemap.io/v3/poi/?output=json&countrycode=IT&maxresults=4&compact=true&verbose=false",
                 listener,
                null);
        queue.add(request);
    }
}
