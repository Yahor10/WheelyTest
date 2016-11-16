package ru.wheely.wheelytest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import app.Constants;
import data.LatLonEntity;
import preferences.PreferenceUtils;
import provider.LocationModel;
import provider.WheelyProvider;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public abstract class BaseFragmentMapActivity extends FragmentActivity
        implements OnMapReadyCallback,UpdateMap {
    public static final String BROADCAST_ERROR_DATA = "BROADCAST_ERROR_DATA";

    protected GoogleMap mMap;
    protected MessageHandler messageHandler = new MessageHandler(this);

    private boolean isResumed = false;
    @Override
    public void updateMap(LatLng latLng) {
        Log.i(Constants.LOG_TAG,"draw new markers..");
        GoogleMap map = getMap();
        ContentResolver contentResolver = getContentResolver();
        if(map != null  && isResumed){
            map.addMarker(new MarkerOptions().position(latLng).title("Marker"));

            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationModel.LAT, latLng.latitude);
            contentValues.put(LocationModel.LON, latLng.longitude);
            contentResolver.insert(WheelyProvider.WHEELY_CONTENT_URI_LOCATION, contentValues);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(Constants.LOG_TAG,"On restart");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void updateLocation(LatLng latLng) {
        Log.i(Constants.LOG_TAG,"draw my location...");
        GoogleMap map = getMap();

        PreferenceUtils.setCurrentLat(this,latLng.latitude);
        PreferenceUtils.setCurrentLon(this,latLng.longitude);

        if(map != null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
    }

    public static final int Marker_Message = 101;
    public static final int MyLocation_Message = 102;

    public static class MessageHandler extends Handler {
        private UpdateMap updateMap;

        public MessageHandler(UpdateMap m) {
            updateMap = m;
        }

        public void clearListener(){
            updateMap = null;
        }

        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;
            if(updateMap == null){
                Log.e(Constants.LOG_TAG,"updatte map is null");
                return;
            }

            switch (state)
            {
                case Marker_Message:
                    String markers = (String) message.obj;
                    Type listType = new TypeToken<ArrayList<LatLonEntity>>(){}.getType();
                    List<LatLonEntity> list = new Gson().fromJson(markers, listType);
                    if(updateMap != null)
                    {
                        for (LatLonEntity e :list)
                        {
                            final LatLng pos = new LatLng(e.getLat(),e.getLon());
                            updateMap.updateMap(pos);
                        }
                    }
                    break;
                case MyLocation_Message:
                    LatLonEntity location = (LatLonEntity) message.obj;
                    updateMap.updateLocation(new LatLng(location.getLat(),location.getLon()));
                    break;
            }
        }
    }

    public GoogleMap getMap() {
        return mMap;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v(Constants.LOG_TAG,"on config changed!");
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

        // Add a marker in Sydney and move the camera
        double currentLat = PreferenceUtils.getCurrentLat(this);
        double currentLon = PreferenceUtils.getCurrentLon(this);

        LatLng me = new LatLng(currentLat,currentLon);
        mMap.addMarker(new MarkerOptions().position(me).title("Its Me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        //if(isMapChanged)
        {
            Log.i(Constants.LOG_TAG,"Redraw markers after config change");
            Cursor cursor = getContentResolver().query(WheelyProvider.WHEELY_CONTENT_URI_LOCATION,
                    WheelyProvider.projection.get(LocationModel.class), null, null, null);
            if (cursor.moveToFirst()){
                do{
                    LocationModel locationModel = new LocationModel(cursor);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(locationModel.getLat(),locationModel.getLon())).title("Marker"));
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
    }

    protected void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseFragmentMapActivity.this,
                                    new String[]{permission}, requestCode);
                        }
                    }, getString(android.R.string.ok), null, getString(android.R.string.cancel));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }


    protected void showAlertDialog(@Nullable String title, @Nullable String message,
                                   @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
                                   @NonNull String positiveText,
                                   @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener,
                                   @NonNull String negativeText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
        builder.show();
    }

    public void showErrorSnackBar(String text){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);
// Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setBackgroundColor(Color.WHITE);//change Snackbar's background color;

        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)layout.getLayoutParams();
        params.gravity = Gravity.TOP;
        layout.setLayoutParams(params);
// Hide the text
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this,android.R.color.holo_blue_light));
// Show the Snackbar
        snackbar.setText(text);
        snackbar.show();
    }
}
