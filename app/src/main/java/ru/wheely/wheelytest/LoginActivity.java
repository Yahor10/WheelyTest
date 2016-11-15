package ru.wheely.wheelytest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.NetworkUtils;

import app.Constants;
import preferences.PreferenceUtils;
import service.BaseWebService;
import service.LoginService;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity  {

    public static final String ACTION_LOGIN_SUCCESS = "ru.wheely.wheelytest.ACTION_LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAILED  = "ru.wheely.wheelytest.ACTION_LOGIN_FAILED";

    public static Intent buildIntent(Context context){
        return new Intent(context,LoginActivity.class);
    }
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;


    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.user);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Context ctx =this;
        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!NetworkUtils.isNetworkAvailable(ctx)){
                    showErrorSnackBar("Network is unavailable");
                }else{
                    mUserView.setText("aaaaaw");// TODO remove test data
                    mPasswordView.setText("aaadaaw");
                    attemptLogin();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        String userName = PreferenceUtils.getUserName(this);
        String userPass = PreferenceUtils.getUserPass(this);

        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPass)) {
            mUserView.setText(userName);
            mPasswordView.setText(userPass);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceUtils.isLogin(this)){
            startActivity(MapsActivity.buildIntent(this));
            overridePendingTransition( R.anim.sliding_up_new, R.anim.sliding_out_up );
            finish();

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
//
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String useer = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(useer)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(useer)) {
            mUserView.setError(getString(R.string.error_invalid_email));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(useer, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String user) {
        return !user.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void handleSuccess(Intent i) {
        if(i.getAction().equals(ACTION_LOGIN_SUCCESS))
        {
            Log.i(Constants.LOG_TAG,"success login,web socket is opened");
            startActivity(MapsActivity.buildIntent(this));
            overridePendingTransition( R.anim.sliding_up_new, R.anim.sliding_out_up );
            finish();
            PreferenceUtils.setLogin(this,true);
        }
    }

    @Override
    protected void handleFail(Intent i) {

        String action = i.getAction();
        if(action.equals(ACTION_ERROR))
        {
            String errMessage = i.getStringExtra(EXTRA_ERROR_MESSAGE);
            if(TextUtils.isEmpty(errMessage))
            {
                errMessage = getString(R.string.error_unexpected);
            }

            showErrorSnackBar(errMessage);
        }else if(action.equals(ACTION_LOGIN_FAILED))
        {
            String errMessage = i.getStringExtra(EXTRA_ERROR_MESSAGE);
            if(TextUtils.isEmpty(errMessage))
            {
                errMessage = getString(R.string.error_login_creds);
            }
            showErrorSnackBar(errMessage);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                Intent service = new Intent(LoginActivity.this, LoginService.class);
                service.setAction(LoginService.ACTION_ATTEMPT_LOGIN);

                service.putExtra(BaseWebService.EXTRA_NAME,mUser);
                service.putExtra(BaseWebService.EXTRA_PASSWORD,mPassword);

                startService(service);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

