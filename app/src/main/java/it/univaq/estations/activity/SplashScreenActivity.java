package it.univaq.estations.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startActivity(new Intent(SplashScreenActivity.this, MapsActivity.class));
            finish();
        }
    }
