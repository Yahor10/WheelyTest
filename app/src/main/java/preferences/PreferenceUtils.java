package preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by CoolerBy on 21.09.2016.
 */
public class PreferenceUtils {
    final static String nameSpace = "wheely";

    public static void setUserName(Context context, String name ){
        SharedPreferences prefs =  context.getSharedPreferences(nameSpace, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.USER_NAME,name);
        editor.commit();
    }

    public static String getUserName(Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(nameSpace, 0);
        return prefs.getString(PreferenceKeys.USER_NAME,"");
    }

    public static void setUserPass(Context context, String pass ){
        SharedPreferences prefs =  context.getSharedPreferences(nameSpace, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.USER_PASSWORD,pass);
        editor.commit();
    }

    public static String getUserPass(Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(nameSpace, 0);
        return prefs.getString(PreferenceKeys.USER_PASSWORD,"");
    }

    public static void setLogin(Context context, Boolean logged ){
        SharedPreferences prefs =  context.getSharedPreferences(nameSpace, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PreferenceKeys.LOGIN,logged);
        editor.commit();
    }

    public static boolean isLogin(Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(nameSpace, 0);
        return prefs.getBoolean(PreferenceKeys.LOGIN,false);
    }
}
