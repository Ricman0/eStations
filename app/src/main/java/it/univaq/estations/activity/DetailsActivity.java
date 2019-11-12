package it.univaq.estations.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;

import java.util.ArrayList;

import it.univaq.estations.database.Database;
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
    private long stationId;
    private Context context;
    Handler mHandler;
    Thread threadToLoadStationFromDB;
    private static final int LOAD_STATION_COMPLETED = 100;
    private String urlImage = "";
    private Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar myToolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setSubtitle(getString(R.string.activity_details_name));

        activity = this;
        context = getApplicationContext();
        managePointsOfChargeRecyclerView();
        mHandler = new MyHandler();

        //get Extras from intent
//        stationId = getIntent().getIntExtra("stationId", -1);
        stationId = getIntent().getLongExtra("stationId", -1);

        System.out.println("detailsActitivyt" + stationId );
        //get station from database
        appDB = Database.getInstance(getApplicationContext());

        threadToLoadStationFromDB = new Thread(new Runnable() {
            @Override
            public void run() {
                station = appDB.getStationDao().getById(stationId);
                System.out.println("threadToLoadStationFromDB id stazione " + station.getId());
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
                int permissionFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
                    // explain to activate permission
                    Context context = getApplicationContext();
                    CharSequence text = getString(R.string.reach_destination);
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                else {
                    reachDestination();
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        if(threadToLoadStationFromDB.isAlive()) {
            threadToLoadStationFromDB.interrupt();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void fillDetailsLayout(){

        //fill the layout with the station data
        TextView stationName = findViewById(R.id.stationNameDetails);
        TextView stationUsageCost = findViewById(R.id.usageCostDetails);
        TextView stationTown = findViewById(R.id.stationTownDetails);
        TextView stationAddress = findViewById(R.id.stationAddressDetails);
        TextView stationUrl = findViewById(R.id.stationUrlDetails);
        TextView stationNumPointsOfCharge = findViewById(R.id.numPointsOfChargeDetails);

        stationName.setText(station.getTitle());
        //TODO mostrare euro se in europa dollaro se in usa e così via
        Object o = station.usageCost();
        if (o instanceof Number) {
            stationUsageCost.setText(o + " €/KWh");
        } else  {
            stationUsageCost.setText((String)o);
        }
        stationTown.setText(station.getTown());
        stationAddress.setText(station.getAddress());
        stationUrl.setText(station.getUrl());
        stationNumPointsOfCharge.setText(String.valueOf(station.getNumberOfPointsOfCharge()));

        //underline the station url only if there is an url
        if(station.getUrl().contentEquals( "No Url"))
        {
            stationUrl.setVisibility(View.GONE);
        }
        else{
            SpannableString content = new SpannableString(station.getUrl());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            stationUrl.setText(content);
        }


        // to reach the web page when touch the link
        stationUrl.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                goToUrl(station.getUrl());
                // return true if you don't want it handled by any other touch/click events after this
                return false;
            }
        });

        // disable navigateToStation button when permission is disabled
        int permissionFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            // non concessi
            ImageButton button = (ImageButton) this.findViewById(R.id.navigateToStation);
            button.setColorFilter(ContextCompat.getColor(context, R.color.disabled_color));
        }
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
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
    }

    /**
     * Function to change station image in the DetailsActivity
     *
     * @param newImage Bitmap Immage to set
     * @author Claudia Di Marco & Riccardo Mantini
     */
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
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        return true;
    }

    /**
     * Function to reach the web page associated to the url
     *
     * @param url The url to reach
     * @author Claudia Di Marco & Riccardo Mantini
     */
    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private class MyHandler extends Handler {

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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_language:
//                // User chose the "Settings" item, show the app settings UI...
//                return true;

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
