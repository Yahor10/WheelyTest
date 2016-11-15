package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocketState;

import app.Constants;
import preferences.PreferenceUtils;
import ru.wheely.wheelytest.BaseActivity;
import ru.wheely.wheelytest.LoginActivity;


public class LoginService extends BaseWebService
{

    public static String ACTION_ATTEMPT_LOGIN = "ru.wheely.wheelytest.ACTION_ATTEMPT_LOGIN ";

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
    protected void handleAction(Intent i) {
        String action = i.getAction();
        Intent intent;
        Log.v(Constants.LOG_TAG,"Handle login service action" + action);
        if(action.equals(ACTION_ATTEMPT_LOGIN))
        {
            WebSocketState serializableExtra = (WebSocketState) i.getSerializableExtra(EXTRA_WEBSOCKET_STATE);

            switch (serializableExtra)
            {
                case OPEN:
                    Log.i(Constants.LOG_TAG,"open socket...");

                    PreferenceUtils.setUserName(this,userName);
                    PreferenceUtils.setUserPass(this,userPass);

                    intent = LoginActivity.buildIntent(this);
                    intent.setAction(LoginActivity.ACTION_LOGIN_SUCCESS);
                    startActivity(intent);

                    break;
                case CLOSED:
                {
                    intent = LoginActivity.buildIntent(this);
                    intent.setAction(LoginActivity.ACTION_LOGIN_FAILED);
                    startActivity(intent);
                }
            }
        }else if(action.equals(BaseActivity.ACTION_ERROR))
        {
            intent = LoginActivity.buildIntent(this);
            intent.setAction(LoginActivity.ACTION_LOGIN_FAILED);

            intent.putExtra(BaseActivity.EXTRA_ERROR_MESSAGE,i.getStringExtra(BaseActivity.EXTRA_ERROR_MESSAGE));
            startActivity(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
