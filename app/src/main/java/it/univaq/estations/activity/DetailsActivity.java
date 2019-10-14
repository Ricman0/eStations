package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.PointOfChargeListAdapter;
import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == LOAD_STATION_COMPLETED) {
                    fillDetailsLayout();
                    managePointsOfChargeRecyclerView();
                }
            }
        };
    }

    @Override
    public void onPause(){
        super.onPause();
        threadToLoadStationFromDB.stop(); //threadToLoadStationFromDB.interrupt(); // better check
    }

    @Override
    public void onResume(){
        super.onResume();
        context = getApplicationContext();

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

        //add click listener to the navigatioToStation button
        ImageButton button = findViewById(R.id.navigateToStation);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.streetview:cbll=46.414382,10.013988");

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            }
        });
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
        pointsOfCharge = station.getPointsOfCharge();

        recyclerView = findViewById(R.id.pointOfCharge_list);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new PointOfChargeListAdapter(context, pointsOfCharge);
        recyclerView.setAdapter(adapter);
    }
}
