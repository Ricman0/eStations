package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.PointOfChargeListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;
import it.univaq.estations.utility.VolleyRequest;

/**
 * Java Class for detail stations
 * */
public class DetailsActivity extends AppCompatActivity {

    private Station station;
    private ArrayList<PointOfCharge> pointsOfCharge = new ArrayList<>();
    private Database appDB;
    private LinearLayoutManager layoutManager;
    private RecyclerView  recyclerView;
    private PointOfChargeListAdapter adapter;
    private String stationId;
    private Context context;
    Handler mHandler;
    Thread threadToLoadStationFromDB;
    private static final int LOAD_STATION_COMPLETED = 100;
    private String urlImage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("E-stations");
        getSupportActionBar().setSubtitle("Station Details");


        context = getApplicationContext();
        managePointsOfChargeRecyclerView();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == LOAD_STATION_COMPLETED) {
                    // get Image from the urlImage and change ImageView element
                    if(urlImage != null ) {
                        VolleyRequest.getInstance(getApplicationContext()).downloadImage(new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                changeImage(response);
                            }
                        }, urlImage);
                    }

                    fillDetailsLayout();
                    pointsOfCharge = station.getPointsOfCharge();
                    adapter.add(pointsOfCharge);
                }
            }
        };



        //get Extras from intent
        stationId = getIntent().getStringExtra("stationId");

        //get station from database
        appDB = Database.getInstance(getApplicationContext());

        threadToLoadStationFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                station = appDB.getStationDao().getById(stationId);
                station.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationId));
                urlImage = station.getStationImageUrl();
                Message message = new Message();
                message.what = LOAD_STATION_COMPLETED;
                mHandler.sendMessage(message);
            }
        });

        threadToLoadStationFromDB.start();

        //add click listener to the navigationToStation button
        ImageButton button = findViewById(R.id.navigateToStation);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reachDestination();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        threadToLoadStationFromDB.interrupt();
    }

    @Override
    public void onResume(){
        super.onResume();
        /*context = getApplicationContext();

        //get Extras from intent
        stationId = getIntent().getStringExtra("stationId");

        //get station from database
        appDB = Database.getInstance(getApplicationContext());

        threadToLoadStationFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                station = appDB.getStationDao().getById(stationId);
                station.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationId));
                Message message = new Message();
                message.what = LOAD_STATION_COMPLETED;
                mHandler.sendMessage(message);
            }
        });

        threadToLoadStationFromDB.start();

        //add click listener to the navigationToStation button
        ImageButton button = findViewById(R.id.navigateToStation);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reachDestination();
            }
        });*/


    }

    public void fillDetailsLayout(){

        //fill the layout with the station data
        TextView stationName = findViewById(R.id.stationNameDetails);
        TextView stationTown = findViewById(R.id.stationTownDetails);
        TextView stationAddress = findViewById(R.id.stationAddressDetails);
        TextView stationUrl = findViewById(R.id.stationUrlDetails);
        TextView stationNumPointsOfCharge = findViewById(R.id.numPointsOfChargeDetails);

        stationName.setText(station.getTitle());
        stationTown.setText(station.getTown());
        stationAddress.setText(station.getAddress());
        stationUrl.setText(station.getUrl());
        stationNumPointsOfCharge.setText(String.valueOf(station.getNumberOfPointsOfCharge()));
    }

    public void managePointsOfChargeRecyclerView(){

        recyclerView = findViewById(R.id.pointOfCharge_list);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new PointOfChargeListAdapter(context, pointsOfCharge);
        recyclerView.setAdapter(adapter);
    }


    public void reachDestination(){
        //In order to launch Google Maps: create an Intent object specifying its action, URI and package.

        String latitude = String.valueOf(station.getPosition().latitude);
        String longitude = String.valueOf(station.getPosition().longitude);

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude+"&mode=d");
        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // will ensure that the Google Maps app for Android handles the Intent.
        mapIntent.setPackage("com.google.android.apps.maps");

        /*
        If the system cannot identify an app that can respond to the intent, estations app may crash.
        For this reason, first verify that a receiving application is installed
        before you present one of these intents to a user.
        To verify that an app is available to receive the intent, call resolveActivity() on your Intent object.
        If the result is non-null, there is at least one app that can handle the intent.
        If the result is null, you should not use the intent and, if possible, you should disable the feature that invokes the intent.
         */
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            //launch the necessary app (in this case Google Maps) and start the corresponding Activity
            startActivity(mapIntent);
        }

    }

    private void changeImage(Bitmap newImage)
    {
        ImageView imageStation = findViewById(R.id.StationImageDetails);
        imageStation.setImageBitmap(newImage);
    }



    /**
     * Function to define a custom behaviour for the actionBar arrow back.
     *
     * @Override
     * @return boolean
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
