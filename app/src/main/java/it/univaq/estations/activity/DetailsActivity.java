package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
    }
}
