package com.example.rafael.boraapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafael.boraapp.login.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AuthTask mAuthTask = null;
    //private UserSignUpTask.UserLoginTaskL

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Session Manager
        session = SessionManager.getSessionManager(getApplicationContext());


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);

        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignUp() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
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
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
            //Como eh SignUp o ultimo parametro eh um pra indicar que ta criando um novo user
            mAuthTask = new AuthTask(email, username, password, 1);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

        //LOGIN VAI SER SO POR USERNAME POR ENQUANTO

        // Reset errors.
        mEmailView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString(); //Vai ser null pq so vai pelo username
        String username = mUsernameView.getText().toString();
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
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //vai ser 0 pq nao ta criando um novo user e so logando
            mAuthTask = new AuthTask(email, username, password, 0);
            mAuthTask.execute((Void) null);
        }
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AuthTask extends AsyncTask<Void, Void, AuthErrorMsgWrapper> {

        private final String mEmail;
        private final String mPassword;
        private final String mUsername;

        //Se for pra SignUp = 1(New user) e Login = 0(No new user)
        private final int mAuthType;


        //Pra a gnt saber se a mensagem de erro tem que ser mostrada no campo do username ou password
        //So esse boolean for falso entao vai ter dado erro no password
        boolean errorInUsernameField;

        //Pra jogar a mensagem de erro no onPostExecute e bota-la no mUsernameView ou mPasswordView
        String messageJSON = null;

        AuthTask(String email, String username, String password, int signUpOrLogin) {
            mEmail = email;
            mPassword = password;
            mUsername = username;
            mAuthType = signUpOrLogin;
        }

        @Override
        protected AuthErrorMsgWrapper doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            if(params.length == 0){
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=3c968409a0474eb891ad0aefc33f04e2");

                URL url = null;
                if(mAuthType == 1){
                    url = new URL("http://10.0.2.2:3000/users/signup");
                }else if(mAuthType == 0){
                    url = new URL("http://10.0.2.2:3000/users/login");
                }



                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");



                JSONObject auth = new JSONObject();

                try {

                    if(mAuthType == 1){
                        auth.put("email", mEmail);
                        auth.put("username", mUsername);
                        auth.put("password", mPassword);
                    }else if(mAuthType == 0){
                        auth.put("username", mUsername);
                        auth.put("password", mPassword);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                OutputStreamWriter wr= new OutputStreamWriter(urlConnection.getOutputStream());
//                wr.write(auth.toString());
//                wr.flush();
//                wr.close();

                byte[] sent_info = auth.toString().getBytes();

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());

                printout.write(sent_info);
                printout.flush();
                printout.close();


//                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = null;
                StringBuffer buffer = null;

                int httpResponseCode = urlConnection.getResponseCode();
                if(httpResponseCode == 200){
                    inputStream = urlConnection.getInputStream();
                    buffer = readBuffer(reader, inputStream);
                }else{
                    //Toast.makeText(getApplicationContext(), "OLha a merda pai, "+httpResponseCode,Toast.LENGTH_SHORT).show();
                    Log.v("BRONCA", "REsponse code: " + httpResponseCode);
                    InputStream errorInputStream = urlConnection.getErrorStream();
                    buffer = readBuffer(reader, errorInputStream);
                }

                forecastJsonStr = buffer.toString();

                Log.v("RESPONSE", "Server JSON REsponse: " + forecastJsonStr);

                //Vou extrair os dados como email e username direto do JSON retornado pelo server no campo "user"
                JSONObject jObject;
                String usernameJSON = null;
                String emailJSON = null;

                AuthErrorMsgWrapper authwrapper = new AuthErrorMsgWrapper();

                try {
                    jObject = new JSONObject(forecastJsonStr);
                    boolean userNotLoggedIn = jObject.isNull("user");

                    /**JSONObject messageObject;
                    **Se nao existir o user no JSON retornado pelo servidor quer dizer que ocorreu algum erro e só foi setado a message
                    **Se tiver sido ao tentar cadastrar(mAuthType == 1):
                     * {message: "Username already taken"}
                     * Se tiver sido ao tentar logar(mAuthType == 0), pode ser:
                     *  {message: 'This username is not registered'}
                     *  {message: 'User exists but wrong password'}
                     * */
                    if(userNotLoggedIn){
                        messageJSON = jObject.getString("message");
                        //So tem como usar o Toast na Main UI Thread, aqui teria que fazer com que rodasse la
                        //Toast.makeText(getApplicationContext(), messageJSON, Toast.LENGTH_LONG).show();

                        //Entao a gnt vai ver se a mensagem de erro eh relacionada com o username ou password e dps setar
                        //os fields deles com erro no UIThread
                        errorInUsernameField = messageJSON.contains("username"); //Se for true entao a mensagem eh sobre o username e vamos setar o erro la

                        authwrapper.errorInUsernameField = errorInUsernameField;
                        authwrapper.result = false;
                    }else{
                        JSONObject userObject = jObject.getJSONObject("user");
                        usernameJSON = userObject.getString("username");
                        emailJSON = userObject.getString("email");

                        session.createLoginSession(usernameJSON, emailJSON);

                        authwrapper.result = true;
                    }


                    // TODO: register the new account here.

                    return authwrapper;

                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }



            } catch (IOException e) {
                Log.e("FUDEU", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //Log.e(LOG_TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(final AuthErrorMsgWrapper wrapper) {
            mAuthTask = null;
            showProgress(false);

            if (wrapper.result) {
                finish();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            } else {

                if(wrapper.errorInUsernameField){
                    mUsernameView.setError(messageJSON);
                    mUsernameView.requestFocus();
                }else{
                    mPasswordView.setError(messageJSON);
                    mPasswordView.requestFocus();
                }

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }


        public StringBuffer readBuffer(BufferedReader reader, InputStream inputStream){

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            return buffer;
        }



    }
    ///FIM DO AUTHTASK

    //So pra ter como passar para o metodo onPOstExecute o booleano pra a gnt saber onde botar o erro na hora q mostrar pro user
    public class AuthErrorMsgWrapper
    {
        public boolean result;
        public boolean errorInUsernameField;
    }


}

