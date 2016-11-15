package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import app.Constants;


public class LoginService extends BaseWebService
{


    public LoginService()
    {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent == null){
            Log.e(Constants.LOG_TAG,"intent is null");
        return Service.START_STICKY;
       }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
