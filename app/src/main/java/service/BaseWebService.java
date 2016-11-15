package service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

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
import ru.wheely.wheelytest.BaseActivity;
import ru.wheely.wheelytest.R;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public class BaseWebService extends Service {

    public static String EXTRA_ISFOREGROUND = "foreground";
    public static String EXTRA_NAME = "login_name";
    public static String EXTRA_PASSWORD = "login_password";

    private final WebSocketAdapter webSocketAdapter = new WebSocketAdapter(){

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            super.onStateChanged(websocket, newState);
            Log.i(Constants.LOG_WEBSOCKET,"on statechanged" + newState.name());
            switch (newState){
                case OPEN:
                    // Save to preff , success start
                    break;
            }
            WheelyApp.checkWebsocketState();
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.e(Constants.LOG_WEBSOCKET,"on onDisconnected");

        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            super.onConnectError(websocket, exception);
            Log.e(Constants.LOG_WEBSOCKET,"on onConnectError");
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            Log.i(Constants.LOG_WEBSOCKET,"on connected");
        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
            Log.e(Constants.LOG_WEBSOCKET,"on onUnexpectedError");        }

        @Override
        public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onCloseFrame(websocket, frame);
            Log.i(Constants.LOG_WEBSOCKET,"on cclose  frame");
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
            super.onError(websocket, cause);
            Log.e(Constants.LOG_WEBSOCKET,"on onError");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean foreground = intent.getBooleanExtra(EXTRA_ISFOREGROUND, false);
        boolean hasErr = false;

        String userName = intent.getStringExtra(EXTRA_NAME);
        String userPass = intent.getStringExtra(EXTRA_PASSWORD);

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

//         new Thread(new Runnable() {
//             @Override
//             public void run() {
//                 try {
//                     WheelyApp.connect(webSocketAdapter,"aaa:aaa");
//                 } catch (IOException e) {
//                     e.printStackTrace();
//
//                 } catch (WebSocketException e) {
//                     e.printStackTrace();
//                 } catch (NoSuchAlgorithmException e) {
//                     e.printStackTrace();
//                 }
//             }
//         }).start(); ;

        if (hasErr) {
            Intent i = new Intent();
            i.setAction(BaseActivity.ACTION_ERROR);
            i.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE, "cannot connect to server");
            i.putExtra(BaseActivity.EXTRA_ERROR_CODE, 1);
            sendError(i);
            return (START_NOT_STICKY);
        } else{
            return (START_STICKY);
        }
    }


    private void sendError(Intent i) {
        // TODO check if app is available
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void startForeground(int notificationId,Intent i){
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
}
