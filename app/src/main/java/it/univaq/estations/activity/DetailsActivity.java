package it.univaq.estations.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.univaq.estations.R;

/**
 * Java Class for detail stations
 * */
public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String stationNameString = getIntent().getStringExtra("stationName");
        String stationTownString = getIntent().getStringExtra("stationTown");

        TextView stationName = findViewById(R.id.stationNameDetails);
        TextView stationTown = findViewById(R.id.stationTownDetails);

        stationName.setText(stationNameString);
        stationTown.setText(stationTownString);
    }
}
