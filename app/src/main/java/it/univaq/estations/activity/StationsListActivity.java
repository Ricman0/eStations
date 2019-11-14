package it.univaq.estations.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.univaq.estations.database.Database;
import it.univaq.estations.R;
import it.univaq.estations.activity.adapter.StationsListAdapter;
import it.univaq.estations.model.Station;


public class StationsListActivity extends AppCompatActivity {

    private List<Station> stations = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StationsListAdapter adapter;

    private Database appDB;
    private MyHandler mHandler;
    private Thread threadToLoadAllStationsFromDB;
    private static final int ALL_STATIONS_LOADED = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setSubtitle(getString(R.string.station_list_name));

        recyclerView = findViewById(R.id.stations_list);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new StationsListAdapter(this, stations, recyclerView);
        recyclerView.setAdapter(adapter);

        appDB = Database.getInstance(getApplicationContext());
        stations = new ArrayList<>();
        mHandler = new MyHandler(adapter, stations);
    }

    /**
     * Function to resume the activity and load all station in the database.
     *
     * @Override
     * @author Claudia Di Marco & Riccardo Mantini
     */
    @Override
    protected void onResume() {
        super.onResume();

        threadToLoadAllStationsFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("threadToLoadAllStationsFromDB");
                stations.clear();
                //get stations and all pointOFCharges from database
                stations.addAll(appDB.getStationDao().getAllStations()); // get all stations without theirs pointOFCharges
                for (int k = 0; k < stations.size(); k++) {
                    Station stationToFill = stations.get(k);
                    stationToFill.addPointOfChargeList(appDB.getPointOfChargeDao().getAllStationPointsOfCharge(stationToFill.getId()));
                }
                Message message = new Message();
                message.what = ALL_STATIONS_LOADED;
                mHandler.sendMessage(message);

                System.out.println("threadToLoadAllStationsFromDB all station loaded");
            }
        });

        threadToLoadAllStationsFromDB.start();
    }

    /**
     * Function to pause the activity and interrupt the threadToLoadAllStationsFromDB thread if it is alive.
     *
     * @Override
     * @author Claudia Di Marco & Riccardo Mantini
     */
    @Override
    protected void onPause() {
        if(threadToLoadAllStationsFromDB!= null && threadToLoadAllStationsFromDB.isAlive()){
            threadToLoadAllStationsFromDB.interrupt();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    /**
     * Function to restart the activity and clear the StationsListAdapter.
     *
     * @Override
     * @author Claudia Di Marco & Riccardo Mantini
     */
    protected void onRestart() {
        super.onRestart();
        adapter.clear();

    }

    @Override
    protected void onStart() {

        super.onStart();
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
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        return true;
    }

    private class MyHandler extends Handler{

        private final StationsListAdapter handlerAdapter;
        private final List<Station> handlerStations;

        public MyHandler(StationsListAdapter adapter, List<Station> stations) {
            handlerAdapter = adapter;
            handlerStations = stations;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ALL_STATIONS_LOADED) {
                adapter.add(stations);
                if(stations.size() == 0 ) {
                    TextView t = findViewById(R.id.no_stations);
                    t.setVisibility(View.VISIBLE);
                }
                System.out.println("handle all station loaded");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_exit:

                finishAffinity();
                System.exit(0);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }




}
