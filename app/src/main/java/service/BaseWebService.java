package service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import app.Constants;
import app.WheelyApp;
import preferences.PreferenceUtils;
import ru.wheely.wheelytest.BaseActivity;
import ru.wheely.wheelytest.MapsActivity;
import ru.wheely.wheelytest.R;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public abstract class BaseWebService extends Service {

    public static String EXTRA_NAME = "login_name";
    public static String EXTRA_PASSWORD = "login_password";
    public static String EXTRA_WEBSOCKET_STATE = "websocket_state";

    private String mAction;

    protected String userName,userPass;

    protected Messenger messageHandler;


    protected final WebSocketAdapter webSocketAdapter = new WebSocketAdapter(){

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            super.onStateChanged(websocket, newState);
            Log.i(Constants.LOG_WEBSOCKET,"on statechanged" + newState.name());
            WheelyApp.checkWebsocketState();

            Intent intent = new Intent();
            intent.setAction(mAction);
            intent.putExtra(EXTRA_WEBSOCKET_STATE,newState);
            handleAction(intent);

        }

        @Override
        public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            Log.i(Constants.LOG_WEBSOCKET,"on onTextFrame");
            super.onTextFrame(websocket, frame);
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            if(messageHandler != null){
                Log.i(Constants.LOG_WEBSOCKET,"on onTextMessage" + text);
                Message message = Message.obtain();
                message.arg1 = MapsActivity.Marker_Message;
                message.obj = text;
                messageHandler.send(message);
            }
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.e(Constants.LOG_WEBSOCKET,"on onDisconnected");

            String localizedMessage = "Disconnect from server";

            Intent intent = new Intent();
            intent.setAction(BaseActivity.ACTION_ERROR);
            intent.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE,localizedMessage);


            String userName = PreferenceUtils.getUserName(BaseWebService.this);
            String userPass = PreferenceUtils.getUserPass(BaseWebService.this);

            WebSocket webSocket = WheelyApp.getWebSocket();
            if(webSocket != null && !TextUtils.isEmpty(userName))
            {
                Toast.makeText(BaseWebService.this,"Trying to recconect!",Toast.LENGTH_SHORT).show();
                WheelyApp.getWebSocket().removeListener(this);
                WheelyApp.getWebSocket().disconnect();
                WheelyApp.connectAsync(this,userName,userPass);
            }else if(webSocket == null && !TextUtils.isEmpty(userName)){
                WheelyApp.connectAsync(this,userName,userPass);
            }else{
                handleAction(intent);
            }
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            super.onConnectError(websocket, exception);
            Log.e(Constants.LOG_WEBSOCKET,"on onConnectError");
            String localizedMessage = exception.getLocalizedMessage();

            Intent intent = new Intent();
            intent.setAction(BaseActivity.ACTION_ERROR);
            intent.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE,localizedMessage);
            handleAction(intent);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            Log.i(Constants.LOG_WEBSOCKET,"on connected");
        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
            Log.e(Constants.LOG_WEBSOCKET,"on onUnexpectedError");
            String localizedMessage = cause.getLocalizedMessage();

            Intent intent = new Intent();
            intent.setAction(BaseActivity.ACTION_ERROR);
            intent.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE,localizedMessage);
            handleAction(intent);
        }

        @Override
        public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onCloseFrame(websocket, frame);
            Log.i(Constants.LOG_WEBSOCKET,"on cclose  frame");
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
            super.onError(websocket, cause);
            Log.e(Constants.LOG_WEBSOCKET,"on onError");
            String localizedMessage = cause.getLocalizedMessage();

            Intent intent = new Intent();
            intent.setAction(BaseActivity.ACTION_ERROR);
            intent.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE,localizedMessage);

            handleAction(intent);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            Log.e(Constants.LOG_TAG," start with null intent"); // TODO checkk err
            //  Caused by: java.lang.NullPointerException
            //at service.BaseWebService.onStartCommand(BaseWebService.java:160)
            // check websocket state
            return START_STICKY;
        }

        mAction = intent.getAction();
        boolean hasErr = false;

        Log.v(Constants.LOG_TAG,"service start with action" + mAction );

        userName = intent.getStringExtra(EXTRA_NAME);
        userPass = intent.getStringExtra(EXTRA_PASSWORD);

        if(TextUtils.isEmpty(userName))
        {
            userName = PreferenceUtils.getUserName(this);
            userPass = PreferenceUtils.getUserPass(this);
        }

        if(mAction.equals(MapService.ACTION_ATTEMPT_GET_LOCATION)) {
            WebSocket webSocket = WheelyApp.getWebSocket();
            if(webSocket != null)
            webSocket.addListener(webSocketAdapter);

            Bundle extras = intent.getExtras();
            messageHandler = (Messenger) extras.get(MapsActivity.MESSENGER);
            startForeground(1, MapsActivity.buildIntent(this));
        }

        try {
            WheelyApp.connectAsync(webSocketAdapter,userName, userPass);
        } catch (IOException e) {
            hasErr = true;
            e.printStackTrace();
        } catch (WebSocketException e) {
            e.printStackTrace();
            hasErr = true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            hasErr = true;
        }
        if (hasErr) {
            sendError(getString(R.string.error_server_connect));
            stopSelf();
            return (START_NOT_STICKY);
        } else{
            return (START_STICKY);
        }
    }


    protected void startForeground(int notificationId,Intent i){
        PendingIntent pintent = PendingIntent.getActivity(this,0,i,0);

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext());

        builder.setContentTitle("Start Map Service")
                .setContentText("Map web service is working...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pintent)
                .setOngoing(true);


        startForeground(notificationId,
                builder.build());
    }

    protected abstract void handleAction(Intent i);

    protected void sendError(String message){
        // TODO check activities
        Intent i = new Intent();
        i.setAction(BaseActivity.ACTION_ERROR);
        i.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE, message);
        i.putExtra(BaseActivity.EXTRA_ERROR_CODE, 1);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
