package ru.wheely.wheelytest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;

import app.Constants;
import app.WheelyApp;
import preferences.PreferenceUtils;
import provider.WheelyProvider;
import service.LoginService;
import service.MapService;

public class MapsActivity extends BaseFragmentMapActivity  {


    public static final String MESSENGER = "MESSENGER";

    private boolean isDBsaved = false;

    public static String ACTION_START_MAP = "ru.wheely.wheelytest.START_MAP";

    private final  int REQUEST_LOCATION_PERMISSION = 111;
    private final int REQUEST_COARSE_PERMISSION = 112;

    public static Intent buildIntent(Context context){
        return new Intent(context,MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_maps);
        toolbar.setTitleTextColor(Color.WHITE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    getString(R.string.permission_location),
                    REQUEST_LOCATION_PERMISSION);

            return;
        }else if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                    getString(R.string.permission_location),
                    REQUEST_COARSE_PERMISSION);
            return;
        }else {
            startMapService();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(Constants.LOG_TAG,"On save instance");
        isDBsaved = false;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isDBsaved = true;
    }

    BroadcastReceiver errorreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // show error
            String errMessage = intent.getStringExtra(BaseActivity.EXTRA_ERROR_MESSAGE);
            showErrorSnackBar(errMessage);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(errorreceiver, new IntentFilter(BROADCAST_ERROR_DATA));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDisconnect(View w){

        WebSocket webSocket = WheelyApp.getWebSocket();
        if(webSocket != null && webSocket.isOpen()) {
            webSocket.disconnect();
            Toast.makeText(this,"You have been disconnect from server",Toast.LENGTH_SHORT).show();
            PreferenceUtils.setLogin(this,false);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(messageHandler != null)
        messageHandler.clearListener();
        messageHandler = null;

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(errorreceiver);
            if(isDBsaved) {
               getContentResolver().delete(WheelyProvider.WHEELY_CONTENT_URI_LOCATION, null, null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            case REQUEST_COARSE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 startMapService();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private  void startMapService(){
        stopService( new Intent(this, LoginService.class));

        Log.i(Constants.LOG_TAG,"start map service");
        Intent service = new Intent(this, MapService.class);
        service.setAction(MapService.ACTION_ATTEMPT_GET_LOCATION);
        service.putExtra(MESSENGER, new Messenger(messageHandler));
        startService(service);

    }
}
