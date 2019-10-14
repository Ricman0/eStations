package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //get Extras from intent
        stationId = getIntent().getStringExtra("stationId");

        //get station from database
        appDB = Database.getInstance(getApplicationContext());
        //station = appDB.getStationDao().getById(stationId);
        //station.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointOfCharges(stationId));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                station = appDB.getStationDao().getById(stationId);
                station.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointOfCharges(stationId));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //fill the layout with the station data
                        TextView stationName = findViewById(R.id.stationNameDetails);
                        TextView stationTown = findViewById(R.id.stationTownDetails);
                        TextView stationAddress = findViewById(R.id.stationAddressDetails);
                        TextView stationUrl = findViewById(R.id.stationUrlDetails);
                        TextView stationNumPointOfCharges = findViewById(R.id.numPointOfChargesDetails);

                        stationName.setText(station.getName());
                        stationTown.setText(station.getTown());
                        stationAddress.setText(station.getAddress());
                        stationUrl.setText(station.getUrl());
                        stationNumPointOfCharges.setText(station.getPointOfCharges().size());
                        pointsOfCharge = station.getPointOfCharges();

                        if(adapter != null) adapter.notifyDataSetChanged();

                    }
                });

            }
        });
        t.start();


        //fill the station points of charge
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView = findViewById(R.id.pointOfCharge_list);
        // specify an adapter (see also next example)
        adapter = new PointOfChargeListAdapter(this, pointsOfCharge);
        recyclerView.setAdapter(adapter);

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
}
