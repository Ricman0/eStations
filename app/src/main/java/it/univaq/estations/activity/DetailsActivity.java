package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import it.univaq.estations.Database.Database;
import it.univaq.estations.R;
import it.univaq.estations.model.Station;

/**
 * Java Class for detail stations
 * */
public class DetailsActivity extends AppCompatActivity {

    private Station station;
    private Database appDB = Database.getInstance(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //get Extras from intent
        String stationNameString = getIntent().getStringExtra("stationName"); //potrei eliminare
        String stationTownString = getIntent().getStringExtra("stationTown"); // potrei eliminare
        String stationId = getIntent().getStringExtra("stationId");

        //get station from database
        station = appDB.getStationDao().getById(stationId);
        station.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointOfCharges(stationId));

        //fill the layout with the station data



        TextView stationName = findViewById(R.id.stationNameDetails);
        TextView stationTown = findViewById(R.id.stationTownDetails);

        stationName.setText(stationNameString);
        stationTown.setText(stationTownString);

        //add click listener to the navigatioToStation button
        Button button = findViewById(R.id.navigateToStation);
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
