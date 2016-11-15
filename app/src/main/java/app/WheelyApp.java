package app;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by CoolerBy on 14.11.2016.
 */


public class WheelyApp extends Application {
    static WebSocket webSocket;
    final static  ExecutorService es = Executors.newSingleThreadExecutor();

    private static final int TIMEOUT = 15000;
    private static final boolean DEVELOPER_MODE = true;

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());

            if(Build.VERSION.SDK_INT >11)
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
            else{
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build());
            }
        }
    }


    public static WebSocket getWebSocket() {

        return webSocket;
    }



    public static WebSocket connect(WebSocketAdapter adapter,String user,String pass) throws IOException, WebSocketException, NoSuchAlgorithmException {
        createSocket(adapter,user,pass);
        return webSocket.connect();
    }

    public static Future<WebSocket> connectAsync(WebSocketAdapter adapter,String user,String pass) throws IOException, WebSocketException, NoSuchAlgorithmException {
        createSocket(adapter,user,pass);
        return webSocket.connect(es);
    }

    public static Future<WebSocket> reconnectAsync() throws IOException, WebSocketException
    {
        webSocket.disconnect();
        return webSocket.connect(es);
    }

    private static void createSocket(WebSocketListener listener,String user,String pass) throws IOException, WebSocketException, NoSuchAlgorithmException {

        if(user == null || pass == null){
            throw new IllegalArgumentException("params are null");
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("ws")
                .authority("mini-mdt.wheely.com")
                .appendQueryParameter("username", user)
                .appendQueryParameter("password", pass);
        String myUrl = builder.build().toString();

        if(webSocket == null) {
            Log.i(Constants.LOG_WEBSOCKET,"create new socket...");
            webSocket = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(myUrl)
                    .addListener(listener);
        }
    }


    public static void setTextMessage(String message){
        //if(getWebSocket() != null &&)
    }
    public  static void checkWebsocketState() throws IOException, WebSocketException {
        WebSocketState state = webSocket.getState();
        switch (state)
        {
            case CLOSED:
                reconnectAsync();
                break;
        }
    }
}
