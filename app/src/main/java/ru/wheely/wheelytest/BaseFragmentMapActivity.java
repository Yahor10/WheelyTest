package ru.wheely.wheelytest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

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

import data.LatLonEntity;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public abstract class BaseFragmentMapActivity extends FragmentActivity
        implements OnMapReadyCallback,UpdateMap {

    protected GoogleMap mMap;
    protected Handler messageHandler = new MessageHandler(this);

    @Override
    public void updateMap(LatLng latLng) {

        GoogleMap map = getMap();
        if(map != null){
            map.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        }
    }

    @Override
    public void updateLocation(LatLng latLng) {
        GoogleMap map = getMap();
        if(map != null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
    }

    public static final int Marker_Message = 101;
    public static final int MyLocation_Message = 102;

    public static class MessageHandler extends Handler {
        private final UpdateMap updateMap;

        public MessageHandler(UpdateMap m) {
            updateMap = m;
        }

        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;

            switch (state)
            {
                case Marker_Message:
                    String markers = (String) message.obj;
                    Type listType = new TypeToken<ArrayList<LatLonEntity>>(){}.getType();
                    List<LatLonEntity> list = new Gson().fromJson(markers, listType);
                    if(updateMap != null)
                    {
                        for (LatLonEntity e :list) {
                            final LatLng pos = new LatLng(e.getLat(),e.getLon());
                            updateMap.updateMap(pos);
                        }
                    }
                    break;
                case MyLocation_Message:
                    String location = (String) message.obj;
                    LatLonEntity latLonEntity = new Gson().fromJson(location, LatLonEntity.class);
                    updateMap.updateLocation(new LatLng(latLonEntity.getLat(),latLonEntity.getLon()));
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
    protected void onStop() {
        super.onStop();
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
}
