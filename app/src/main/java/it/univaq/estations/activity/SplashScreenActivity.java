package it.univaq.estations.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import it.univaq.estations.Database.Database;

public class SplashScreenActivity extends AppCompatActivity {
    Thread threadDeleteStationFromDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        threadDeleteStationFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                Database.getInstance(getApplicationContext()).getStationDao().deleteAll();
            }
        });
        threadDeleteStationFromDB.start();

        startActivity(new Intent(SplashScreenActivity.this, MapsActivity.class));
        finish();
    }
}
