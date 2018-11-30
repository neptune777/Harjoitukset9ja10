package com.example.android.harjoitukset9ja10;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.harjoitukset9ja10.data.DatabaseContentAdapter;
import com.example.android.harjoitukset9ja10.data.DatabaseContract;
import com.example.android.harjoitukset9ja10.data.OmaSQLiteHelper;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity   implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    private Location mCurrentBestLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;
    private TextView locationTextView;
    private TextView introTextView;
    private TextView mRequests;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Context context;
    private Button haePaikkaButton;

    private int seconds;
    private RecyclerView mNumbersList;
    private SQLiteDatabase mDb;
    private OmaSQLiteHelper dbHelper;
    private DatabaseContentAdapter mAdapter;
    private static final int SQLITE_SEARCH_LOADER = 22;
    private static final int SQLITE_INSERT_LOADER = 23;
    private static final int SQLITE_DELETE_LOADER = 24;
    private static final int SQLITE_DELETE_BY_ID_LOADER = 25;
    private static final int SQLITE_SORT_LOADER = 26;
    private static final String QUERY_EXTRA = "query";
    private static final String SORT_EXTRA = "sort";
    private static final String INSERT_LATITUDE_EXTRA = "insert_latitude";
    private static final String INSERT_LONGITUDE_EXTRA = "insert_longitude";
    private static final String INSERT_ACCURACY_EXTRA = "insert_accuracy";
    private static final String INSERT_PROVIDER_EXTRA = "insert_provider";
    private static final String INSERT_TIME = "insert_time";
    private static final String DELETE_EXTRA = "delete";
    private static final String DELETE_BY_ID_EXTRA = "delete_by_id";
    private ProgressBar mLoadingIndicator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;
        seconds=0;

        mNumbersList = (RecyclerView) findViewById(R.id.recyclerView2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersList.setLayoutManager(layoutManager);
        mNumbersList.setHasFixedSize(true);


        locationTextView = findViewById(R.id.textView);
        introTextView    = findViewById(R.id.textView2);
        mRequests        = findViewById(R.id.textView3);
        haePaikkaButton  = findViewById(R.id.button);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.progressBar);
        //mRequests.setText("Seconds " + seconds);
        dbHelper = new OmaSQLiteHelper(this);
        mDb = dbHelper.getWritableDatabase();







        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        android.support.v4.app.LoaderManager.getInstance(this).initLoader(SQLITE_SEARCH_LOADER, null, this).forceLoad();

        getLocationFromDatabase();





        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("lokapaikka", ("paikka on muuttunut "+location.getLatitude()+", "+location.getLongitude()));


                if(isBetterLocation(mLocation,location)){
                    deleteAllFromDatabase();
                    addNewLocation(""+location.getLatitude(),""+location.getLongitude(),""+location.getAccuracy(),""+location.getProvider(),""+ location.getTime());


                }


                if(mLocation!=null && location.getAccuracy() != mLocation.getAccuracy()) {
                    mLocation = location;         // tallennetaan myöhempää käyttöä varten
                    seconds += 1;
                    mRequests.setText("Paikanmäärityksiä " + seconds);
                    locationTextView.setText("Accuracy " + mLocation.getAccuracy());
                    mCurrentBestLocation = mLocation;
                    Log.d("Provider1", (""+location.getProvider()));
                }else if(mLocation==null){
                    Log.d("onLocationChanged ", ("Pröööt"));
                    mLocation=location;
                    seconds += 1;
                    mRequests.setText("Paikanmäärityksiä " + seconds);
                    locationTextView.setText("Accuracy " + mLocation.getAccuracy());
                    Log.d("Provider12", (""+location.getProvider()));
                }
               // mLocation.getProvider();
                deleteAllFromDatabase();
                addNewLocation(""+location.getLatitude(),""+location.getLongitude(),""+location.getAccuracy(),""+location.getProvider(),""+ location.getTime());
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



        aloita();


    }

    private void getLocationFromDatabase(){

        Bundle queryBundle = new Bundle();
        queryBundle.putString(QUERY_EXTRA,"getLocation");
        Log.d("getLocationFromDatabase", (""));
        LoaderManager loaderManager =  android.support.v4.app.LoaderManager.getInstance(this);
        Loader<Cursor> queryLoader = loaderManager.getLoader(SQLITE_SEARCH_LOADER);

        if (queryLoader == null) {
            loaderManager.initLoader(SQLITE_SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(SQLITE_SEARCH_LOADER, queryBundle, this);
        }

    }

    private void deleteAllFromDatabase(){

        Bundle queryBundle = new Bundle();
        queryBundle.putString(DELETE_EXTRA,"delete");
        Log.d("deleteFromDatabase", "");
        LoaderManager loaderManager =  android.support.v4.app.LoaderManager.getInstance(this);
        Loader<Cursor> queryLoader = loaderManager.getLoader(SQLITE_DELETE_LOADER);

        if (queryLoader == null) {
            loaderManager.initLoader(SQLITE_DELETE_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(SQLITE_DELETE_LOADER, queryBundle, this);
        }

    }


    private void addNewLocation(String latitude, String longitude, String accuracy, String provider, String time) {

        Bundle bundle = new Bundle();
        bundle.putString(INSERT_LATITUDE_EXTRA,latitude);
        bundle.putString(INSERT_LONGITUDE_EXTRA,longitude);
        bundle.putString(INSERT_ACCURACY_EXTRA,accuracy);
        bundle.putString(INSERT_PROVIDER_EXTRA,provider);
        bundle.putString(INSERT_TIME,time);

        LoaderManager loaderManager =  android.support.v4.app.LoaderManager.getInstance(this);
        Loader<Cursor> queryLoader = loaderManager.getLoader(SQLITE_INSERT_LOADER);

        if (queryLoader == null) {
            loaderManager.initLoader(SQLITE_INSERT_LOADER, bundle, this);
        } else {
            loaderManager.restartLoader(SQLITE_INSERT_LOADER, bundle, this);
        }

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

                       locationTextView.setText("Accuracy " + mLocation.getAccuracy());
                       mCurrentBestLocation=mLocation;
                   }else{
                       locationTextView.setText("Paikkatieto ei vielä valmis... odota, ole hyvä");
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


    protected Cursor cursor(String clause){

        switch (clause){

            case DatabaseContract.DatabaseEntry._ID:

                return mDb.query(
                        DatabaseContract.DatabaseEntry.TABLE_LOCATIONS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseContract.DatabaseEntry._ID
                );


            case DatabaseContract.DatabaseEntry.COLUMN_ACCURACY:

                return mDb.query(
                        DatabaseContract.DatabaseEntry.TABLE_LOCATIONS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseContract.DatabaseEntry.COLUMN_ACCURACY

                );
        }
        return null;
    }

    public void open() throws SQLException {
        mDb = dbHelper.getWritableDatabase();
        getLocationFromDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    @Override
    protected void onResume() {
        open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        close();
        super.onPause();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable final Bundle bundle) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            @Override
            public void onStartLoading(){
                super.onStartLoading();


                if(mTaskData != null){
                    deliverResult(mTaskData);
                }else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }


            @Nullable
            @Override
            public Cursor loadInBackground() {

                String QueryString="";
                String InsertLatitudeString="";
                String InsertLongitudeString="";
                String InsertAccuracyString="";
                String InsertProviderString="";
                String InsertTimeString="";
                String DeleteString="";
                int DeleteByIdInt;
                String SortString="";
                boolean isNull;
                boolean isEmpty;
              /*  Log.d("isNull? ",""+isNull);
                Log.d("isEmpty? ",""+isEmpty);
                Log.d("SortString!! ",""+SortString);
*/
              try{

                   QueryString = bundle.getString(QUERY_EXTRA);
                   InsertLatitudeString = bundle.getString(INSERT_LATITUDE_EXTRA);
                   InsertLongitudeString = bundle.getString(INSERT_LONGITUDE_EXTRA);
                   InsertAccuracyString = bundle.getString(INSERT_ACCURACY_EXTRA);
                   InsertProviderString = bundle.getString(INSERT_PROVIDER_EXTRA);
                   InsertTimeString = bundle.getString(INSERT_TIME);
                   DeleteString = bundle.getString(DELETE_EXTRA);
                   DeleteByIdInt = bundle.getInt(DELETE_BY_ID_EXTRA);
                   SortString = bundle.getString(SORT_EXTRA);
                   isNull = SortString==null;
                   isEmpty = TextUtils.isEmpty(SortString);



              }catch(NullPointerException e){

              }



                if(QueryString!=null && !TextUtils.isEmpty(QueryString)){

                    try {

                        return cursor(DatabaseContract.DatabaseEntry._ID);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        return null;
                    }

                }else if(InsertLatitudeString!=null && InsertLongitudeString!=null && !TextUtils.isEmpty(InsertLatitudeString) && !TextUtils.isEmpty(InsertLongitudeString) &&
                        InsertAccuracyString!=null && InsertProviderString!=null && !TextUtils.isEmpty(InsertAccuracyString) && !TextUtils.isEmpty(InsertProviderString)
                        && InsertTimeString!=null && !TextUtils.isEmpty(InsertTimeString) ){

                    try {

                        ContentValues cv = new ContentValues();
                        cv.put(DatabaseContract.DatabaseEntry.COLUMN_LATITUDE, InsertLatitudeString);
                        cv.put(DatabaseContract.DatabaseEntry.COLUMN_LONGITUDE, InsertLongitudeString);
                        cv.put(DatabaseContract.DatabaseEntry.COLUMN_ACCURACY, InsertAccuracyString);
                        cv.put(DatabaseContract.DatabaseEntry.COLUMN_PROVIDER, InsertProviderString);
                        cv.put(DatabaseContract.DatabaseEntry.COLUMN_TIME, InsertTimeString);

                        mDb.insert(DatabaseContract.DatabaseEntry.TABLE_LOCATIONS, null, cv);

                        return cursor(DatabaseContract.DatabaseEntry._ID);





                    }catch (NullPointerException e){
                        e.printStackTrace();
                        return null;
                    }


                }else if(DeleteString!=null && !TextUtils.isEmpty(DeleteString) ){

                    try {



                        mDb.delete(DatabaseContract.DatabaseEntry.TABLE_LOCATIONS, null, null);


                        return cursor(DatabaseContract.DatabaseEntry._ID);

                    }catch(NullPointerException e){
                        e.printStackTrace();
                        return null;
                    }

                }else if(SortString!=null && !TextUtils.isEmpty(SortString)){


                    try {
                        return cursor(DatabaseContract.DatabaseEntry.COLUMN_ACCURACY);
                    }catch(NullPointerException e){
                        e.printStackTrace();
                        return null;
                    }
                }

                return null;
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
               mTaskData = data;

                super.deliverResult(data);
            }

        };
    }
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {


        mAdapter = new DatabaseContentAdapter(this, cursor, mLocation);
        mNumbersList.setAdapter(mAdapter);
        mLoadingIndicator.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }


}
