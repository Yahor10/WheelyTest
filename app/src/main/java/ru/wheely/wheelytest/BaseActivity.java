package ru.wheely.wheelytest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.Constants;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String ACTION_ERROR = "ru.wheely.wheelytest.ACTION_ERROR";
    public static final String EXTRA_ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String EXTRA_ERROR_CODE = "ERROR_CODE";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.LOG_TAG,"on create intent");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(Constants.LOG_TAG,"on new intent invoke");
        String action = intent.getAction();
        if(intent != null && action.equals(ACTION_ERROR)){
            handleFail(intent);
        }
        if(action.equals(LoginActivity.ACTION_LOGIN_SUCCESS)){
            handleSuccess(intent);
        }else if(action.equals(LoginActivity.ACTION_LOGIN_FAILED)){
            handleFail(intent);
        }

    }

    public void startAction(Intent i){
        checkIntent(i);
    }


    protected void checkIntent(Intent i){
        if(i == null){
            throw new IllegalArgumentException("empty intent");
        }
        if(TextUtils.isEmpty(i.getAction())){
            throw new IllegalArgumentException("empty action intent");
        }
    }


    protected abstract void handleSuccess(Intent i);
    protected abstract void handleFail(Intent i);

    public void showErrorSnackBar(String text){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);
// Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setBackgroundColor(Color.WHITE);//change Snackbar's background color;

        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)layout.getLayoutParams();
        params.gravity = Gravity.TOP;
        layout.setLayoutParams(params);
// Hide the text
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this,android.R.color.holo_blue_light));
// Show the Snackbar
        snackbar.setText(text);
        snackbar.show();
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

    protected void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this,
                                    new String[]{permission}, requestCode);
                        }
                    }, getString(android.R.string.ok), null, getString(android.R.string.cancel));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }
}
