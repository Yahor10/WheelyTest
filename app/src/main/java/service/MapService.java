package service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import app.WheelyApp;
import data.LatLonEntity;
import preferences.PreferenceUtils;
import ru.wheely.wheelytest.BaseActivity;
import ru.wheely.wheelytest.MapsActivity;
import ru.wheely.wheelytest.R;

public class MapService extends BaseWebService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static String ACTION_ATTEMPT_GET_LOCATION = "ru.wheely.wheelytest.ACTION_ATTEMPT_GET_LOCATION ";


    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    double lat, lon;

    short connectSuspendCount =0;
    private Gson gson;

    public MapService()
    {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        gson = new Gson();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void handleAction(Intent i) {
        String action = i.getAction();
        Intent intent;
        if(action.equals(BaseActivity.ACTION_ERROR))
        {
            // TODO send error to mapactivity
        }
    }

    void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"Application has not permission access",Toast.LENGTH_SHORT).show();
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();

            WebSocket webSocket = WheelyApp.getWebSocket();
            try {
                if(webSocket == null)
                {
                    String userName = PreferenceUtils.getUserName(this);
                    String userPass = PreferenceUtils.getUserPass(this);
                    WheelyApp.connectAsync(webSocketAdapter,userName,userPass);
                }else if(webSocket != null && webSocket.isOpen())
                {
                    String s = gson.toJson(new LatLonEntity(lat, lon), LatLonEntity.class);

                    WheelyApp.setTextMessage(s);
                    startForeground(1, MapsActivity.buildIntent(this));
                }else if(webSocket != null  && webSocket.getState() == WebSocketState.CLOSED)
                {
                    sendError(getString(R.string.error_server_connect));
                }

            } catch (IOException e) {
                e.printStackTrace();
                sendError(e.getLocalizedMessage());
            } catch (WebSocketException e) {
                e.printStackTrace();
                sendError(e.getLocalizedMessage());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                sendError(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if(connectSuspendCount <3) {
            stopSelf();
            startService(new Intent(this, MapService.class));// restart
            connectSuspendCount++;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Cannot find location .Connection failed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Message message = Message.obtain();
        message.arg1 = MapsActivity.MyLocation_Message;
        LatLonEntity latLonEntity = new LatLonEntity(location.getLatitude(), location.getLongitude());
        message.obj = latLonEntity;
        try {
            if(messageHandler != null)
                messageHandler.send(message);
            WheelyApp.setTextMessage(gson.toJson(latLonEntity));
        } catch (RemoteException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null)
        mGoogleApiClient.disconnect();
    }
}
