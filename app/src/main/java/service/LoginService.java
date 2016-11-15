package service;

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
    protected void handleAction(Intent i) {
        String action = i.getAction();
        Intent intent;
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(LoginActivity.ACTION_LOGIN_SUCCESS);
                    startActivity(intent);
                    break;
                case CLOSED:
                {
                    intent = LoginActivity.buildIntent(this);
                    intent.setAction(LoginActivity.ACTION_LOGIN_FAILED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
