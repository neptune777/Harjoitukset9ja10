package com.example.android.harjoitukset9ja10;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity{

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;
    private TextView locationTextView;
    private TextView accuracyTextView;
    private TextView timeTextView;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Context context;
    private Button haePaikkaButton;
    private Button lopetaButton;

    private boolean onOff;

    private static final String LOCATION_EXTRA = "query";
    private static final String ONOFF_EXTRA = "onoff";
    private static final String LOCATIONUPDATES_EXTRA = "locUpdates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;


        locationTextView    = findViewById(R.id.textView);
        accuracyTextView    = findViewById(R.id.textView2);
        timeTextView        = findViewById(R.id.textView3);
        haePaikkaButton     = findViewById(R.id.button);
        lopetaButton        = findViewById(R.id.button2);

        haePaikkaButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onOff=true;
                aloita();
                haePaikkaButton.setVisibility(View.INVISIBLE);
                lopetaButton.setVisibility(View.VISIBLE);
            }
        });

        lopetaButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onOff=false;
                lopeta();
                haePaikkaButton.setVisibility(View.VISIBLE);
                lopetaButton.setVisibility(View.INVISIBLE);
            }
        });

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable(LOCATION_EXTRA);
            onOff = savedInstanceState.getBoolean(ONOFF_EXTRA);
            locationTextView.setText("Latitudi: " + mLocation.getLatitude() + ", Longitudi: " + mLocation.getLongitude());
            accuracyTextView.setText("Paikannuksen tarkkuus: " + mLocation.getAccuracy());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(mLocation.getTime());
            String formattedTime = format.format(date);
            timeTextView.setText("Paikannusaika: " + formattedTime);
           }

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("lokapaikka", ("paikka on muuttunut "+location.getLatitude()+", "+location.getLongitude()));


                if(isBetterLocation(location, mLocation)){

                    mLocation = location;         // tallennetaan myöhempää käyttöä varten
                    locationTextView.setText("Latitudi: " + mLocation.getLatitude() + ", Longitudi: " + mLocation.getLongitude());
                    accuracyTextView.setText("Paikannuksen tarkkuus: " + mLocation.getAccuracy());
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date(mLocation.getTime());
                    String formattedTime = format.format(date);
                    timeTextView.setText("Paikannusaika: " + formattedTime);


                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(onOff){
            haePaikkaButton.setVisibility(View.INVISIBLE);
            lopetaButton.setVisibility(View.VISIBLE);
            aloita();
        }else{

            haePaikkaButton.setVisibility(View.VISIBLE);
            lopetaButton.setVisibility(View.INVISIBLE);

        }




    }


    public void lopeta(){
        // Remove the listener you previously added
        mLocationManager.removeUpdates(mLocationListener);
    }


    public void aloita(){

               try {
                   //tarkistetaan lupa
                   kysyLupaa(context);
                   //Huonossa paikassa hidas haku
                   mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                   //Ottaa verkon paikan, joten yleensä nopea tapa hakea joku sijainti
                   mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);





                   if(mLocation!=null ) {

                       locationTextView.setText("Latitudi: " + mLocation.getLatitude() + ", Longitudi: " + mLocation.getLongitude());
                       accuracyTextView.setText("Paikannuksen tarkkuus: " + mLocation.getAccuracy());

                   }else{
                       locationTextView.setText("Paikkatiedot eivät vielä valmiita... odota, ole hyvä");
                   }
               }catch (SecurityException e){
                   Log.d("lokasofta", "Virhe: Sovelluksella ei ollut oikeuksia lokaatioon");
               }





    }

    public boolean kysyLupaa(final Context context){
        Log.d("lokasofta", "kysyLupaa()");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("lokasofta", " Permission is not granted");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("lokasofta", "Kerran kysytty, mutta ei lupaa... Nyt ei kysytä uudestaan");

            } else {
                Log.d("lokasofta", " Request the permission");
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
            return false;
        } else {

            Log.d("lokasofta", "Permission has already been granted");
            return true;
        }

    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("lokasofta ", "onRequestPermissionsResult()");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("lokasofta", "lupa tuli!");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d("lokasofta", "Haetaan paikkaa tietyin väliajoin");
                        //Request location updates:
                        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("lokasofta", "Ei tullu lupaa!");
                }
                return;
            }

        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);


    }





    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLocation!=null) {
          outState.putParcelable(LOCATION_EXTRA, mLocation);
        }
        outState.putBoolean(ONOFF_EXTRA,onOff);

    }


}
