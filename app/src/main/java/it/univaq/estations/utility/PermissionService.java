package it.univaq.estations.utility;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionService{
    //public class PermissionService extends IntentService{

    public static final String PERMISSION_GRANTED = "PermissionService_PermissionGranted";

    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 3;

    private static boolean fineLocationPermissionGranted = false;
    private static boolean coarseLocationPermissionGranted = false;
    private static boolean internetPermissionGranted = false;

    private static PermissionService instance = null;



    public static PermissionService getInstance(){
        return instance == null ? instance = new PermissionService() : instance;
    }

    public void permissionsCheck(Context context, Activity activity){

        //check if the user granted the permission yet

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // permission is not granted yet

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        }
        else{
            coarseLocationPermissionGranted = true;
        }

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // permission is not granted yet

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        }
        else{
            fineLocationPermissionGranted = true;
        }

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED)
        {
            // permission is not granted yet

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);

                // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        }
        else
        {
            internetPermissionGranted = true;
        }

    }




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setInternetPermissionGranted(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setCoarseLocationPermissionGranted(true);
                }

            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setFineLocationPermissionGranted(true);
                    Intent i = new Intent(PERMISSION_GRANTED);
                    //todo: cancellare??
                    //sendBroadcast(i);

                   // updateLocationUI(); funzione in MapsActivity
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    public static boolean isFineLocationPermissionGranted() {
        return fineLocationPermissionGranted;
    }

    public static void setFineLocationPermissionGranted(boolean fineLocationPermissionGranted) {
        PermissionService.fineLocationPermissionGranted = fineLocationPermissionGranted;
    }

    public static boolean isCoarseLocationPermissionGranted() {
        return coarseLocationPermissionGranted;
    }

    public static void setCoarseLocationPermissionGranted(boolean coarseLocationPermissionGranted) {
        PermissionService.coarseLocationPermissionGranted = coarseLocationPermissionGranted;
    }

    public static boolean isInternetPermissionGranted() {
        return internetPermissionGranted;
    }

    public static void setInternetPermissionGranted(boolean internetPermissionGranted) {
        PermissionService.internetPermissionGranted = internetPermissionGranted;
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//
//    }
}
