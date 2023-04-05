package com.brav.primisphoto.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

public class GPSManager2 implements LocationListener{

    public static boolean isIsEnable() {
        return isEnable;
    }
    private static Context context;


    private static boolean isEnable = false;
    private static boolean startRequestLocation = false;
    private static Location mPosition = null;
    private static Location lastPosition = null;
    private static boolean isAsking = false;
    private static LocationManager locationManager;
    public int distanceInThisSession = 0;

    /**
     * Settings.getGeoreferenzia()
     *
     * * usato puntualmente in
     * SEGNALAZIONI
     * AUDIO
     * EASYTICKET
     * VERBALE
     * RAPIDO
     *
     *
     * SettingsGPS.getEnable()
     *
     * usato nello SCATSERVICES per fare l'update delle posizioni del percorso
     * usato nel MENU e nel LOGIN
     *
     */


    /**
     * The internal name of the provider for the coarse location
     */
    private static final String PROVIDER_COARSE = LocationManager.NETWORK_PROVIDER;
    /**
     * The internal name of the provider for the fine location
     */
    private static final String PROVIDER_FINE = LocationManager.GPS_PROVIDER;
    /**
     * The internal name of the provider for the fine location in passive mode
     */
    private static final String PROVIDER_FINE_PASSIVE = LocationManager.PASSIVE_PROVIDER;


    //INTERVAL_DEFAULT Nico l'aveva settato a 5000 cioè 5 secondi ma ammazza la batteria!!!
    // l'ho impostato a 2 minuti
    // Hunter e follower hanno invece il consumo di batteria maggiore e lo fa più spesso!
    private static long INTERVAL_DEFAULT = 2 * 60 * 1000;
    private static final long INTERVAL_FAST = 0; //3 * 60 * 1000; // il più veloce che può
    private static final long MIN_METER = 30;
    private static final long MIN_METER_FAST = 0;

    public final static int CODE_GPS_PERMISSION = 11;

    private static GPSManager2 ourInstance = null;

    public static void resetAsk(){
        isAsking = false;
    }

    public static GPSManager2 getInstance(Context ctx) {


        if(ctx instanceof Activity &&  !isAsking ){
            if( !checkPermission(ctx))
                requestPermission(ctx, CODE_GPS_PERMISSION);
        }

        if(ourInstance == null){
            context = ctx;
            ourInstance = new GPSManager2(context);
        }

        if(!startRequestLocation){
            ourInstance.startLocationUpdate(context);
        }

        return ourInstance;
    }

    public static void openSettings(final Context context) {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }


    private GPSManager2(Context ctx) {
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if(!checkPermission(ctx) && ctx instanceof Activity){
            requestPermission(ctx, CODE_GPS_PERMISSION);
        }else if(checkPermission(ctx)){
            isEnable = true;
            this.startLocationUpdate(ctx);
        }
    }



    public void startLocationUpdate(Context context){
        if(checkPermission(context)) {
            startRequestLocation = true;
            Log.v("startLocationUpdate","startLocationUpdate");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL_DEFAULT, MIN_METER, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL_DEFAULT, MIN_METER, this);
        }
    }


    public void removeUpdate(){
        startRequestLocation = false;
        locationManager.removeUpdates(this);
    }



    public Location getPosition()
    {
        return mPosition;
    }

    public static boolean checkPermission(Context ctx){
        int result1 = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }


    private static void requestPermission(Context ctx, int code){
        //if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) ctx  , Manifest.permission.ACCESS_FINE_LOCATION)){
        //    Toast.makeText(ctx, "This app relies on location data for it's main functionality. Please enable GPS data to access all features.", Toast.LENGTH_LONG).show();
        //} else {
        isAsking = true;
        ActivityCompat.requestPermissions((Activity) ctx,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION
            }, code);
        //}
    }

    //CHIAMATO quando scatta in automatico il cambio di posizione!

    @Override
    public void onLocationChanged(Location location) {
        Log.v("onLocationChanged","onLocationChanged " );
        mPosition = location;


        if (isLocationEnabled(context)) {
            Location pos = GPSManager2.getInstance(context).getPosition();
            if (pos != null && pos.getAccuracy() < 40) {

                double distance = 0;
                if (lastPosition != null)
                    distance = lastPosition.distanceTo(pos);

                //Per me le posizione buone sono solo se la distanza è > di getGeocodeDistanceM()
                if (lastPosition == null || distance > 50) {
                    distanceInThisSession += distance;
                    lastPosition = pos;
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
}
