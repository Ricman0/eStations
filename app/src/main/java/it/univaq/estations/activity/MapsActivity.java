package it.univaq.estations.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Station> stations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //Objects.requireNonNull(mapFragment).getMapAsync(this);

//add click listener to the navigationToStation button
        ImageView icon = findViewById(R.id.iconToStationListActivity);
        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Open another Activity and pass to it the right station
                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(v.getContext(), StationsList.class);

                //add extras to intent
                //Station station = mDataset.get(getAdapterPosition());
                //intent.putExtra("stationId", station.getId());

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng laquila = new LatLng(42.367422, 13.349200);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(laquila).title("Marker in L'Aquila"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(laquila));
    }
}
