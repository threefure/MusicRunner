package com.amk2.musicrunner.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.constants.StatusCode;
import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.amk2.musicrunner.utilities.RegisterValidator;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.SharedPreferencesUtility;
import com.amk2.musicrunner.utilities.StringLib;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener{
    public static final String LOGIN = "login";
    public static final String STATUS = "status";
    public static final int STATUS_NONE = 0;
    public static final int STATUS_LOGIN = 1;
    public static final int STATUS_LOGOUT = 2;
    public static final int MUSIC_RUNNER_MAIN_REQUEST = 100;
    public static final int FACEBOOK_LOGIN_REQUEST = 101;

    private Button skipButton;
    private SharedPreferences loginSharedPreferences;
    private int loginStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginSharedPreferences = getSharedPreferences(LOGIN, MODE_PRIVATE);
        loginStatus = loginSharedPreferences.getInt(STATUS, STATUS_NONE);
        if (loginStatus == STATUS_NONE) {
            initViews();
            setViews();

            //if sharedpreference has value, then login automatically
            String userAccount = SharedPreferencesUtility.getAccount(this);
            if (StringLib.hasValue(userAccount)) {
                Intent redirectIntent = new Intent(this, MusicRunnerActivity.class);
                startActivityForResult(redirectIntent, MUSIC_RUNNER_MAIN_REQUEST);
                finish();
            }
        } else {
            Intent intent = new Intent(this, MusicRunnerActivity.class);
            startActivity(intent);
            finish();
        }

        /*PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(
                    "com.amk2.musicrunner",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/

    }

    private void initViews () {
        skipButton = (Button) findViewById(R.id.login_skip);
    }

    private void setViews () {
        skipButton.setOnClickListener(this);
    }

    public void facebookLogin(View view){
        Intent intent = new Intent(this, FBLogin.class);
        startActivityForResult(intent, FACEBOOK_LOGIN_REQUEST);
    }

    public void registerAccount(View view) {

        // Do something in response to button
        EditText accountEditText = (EditText) findViewById(R.id.account_info);
        String account = accountEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.password);
        String password = passwordEditText.getText().toString();

        if(RegisterValidator.validateAccount(account.trim()) == false){
            TextView statusEditText = (TextView)findViewById(R.id.loginPageStatus);
            statusEditText.setText("Invalid username : username cannot have whitespace", TextView.BufferType.EDITABLE);
        }else if(RegisterValidator.validatePassword(password) == false){
            TextView statusEditText = (TextView)findViewById(R.id.loginPageStatus);
            statusEditText.setText("Invalid password: password cannot be empty", TextView.BufferType.EDITABLE);
        }else{
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("userAccount", account));
            pairs.add(new BasicNameValuePair("password",password));
            HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.REGISTER_ENDPOINT, pairs);
            setStatusToEditText(response);
        }
    }

    public void loginAccount(View view) {
        /*
        EditText accountEditText = (EditText) findViewById(R.id.account_info);
        String account = accountEditText.getText().toString().trim();

        EditText passwordEditText = (EditText) findViewById(R.id.password);
        String password = passwordEditText.getText().toString();

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("userAccount", account));
        pairs.add(new BasicNameValuePair("password",password));

        HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.LOGIN_ENDPOINT,pairs);
        boolean isSuccessful = setStatusToEditText(response);

        if(isSuccessful){
            SharedPreferences preferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
            preferences.edit().putString(Constant.ACCOUNT_PARAMS, account).commit();
            Intent intent = new Intent(this, MusicRunnerActivity.class);
            startActivityForResult(intent,MUSIC_RUNNER_MAIN_REQUEST);
        }*/

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.login, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getStatusCode(HttpResponse response){
        if(response == null)
            return StatusCode.NO_RESPONSE;
        StringBuilder sb = new StringBuilder();
        String line = "";
        try{
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException io){

        }
        return sb.toString();
    }

    public String getStatusMessage(String statusCode) {
        if(StatusCode.NO_USER.equals(statusCode)){
            return "No Such User";
        } else if (StatusCode.NO_RESPONSE.equals(statusCode)){
            return "Cannot get information from Server, please check your internet connection and try again later.";
        } else if (StatusCode.WRONG_PASSWORD.equals(statusCode)){
            return "Password is not correct";
        } else if (StatusCode.REGISTER_SUCCESSFULLY.equals(statusCode)){
            return "Register Successfully";
        } else if (StatusCode.REGISTER_FAIL.equals(statusCode)) {
            return "Register fails";
        } else if (StatusCode.FAIL_TO_LOGIN.equals(statusCode)){
            return "Fail to Login";
        } else if (StatusCode.DUPLICATE_ACCOUNT.equals(statusCode)){
            return "Account has already been used, please choose another account name";
        } else {
            return "Login Successfully";
        }
    }

    //return true if users successfully login/register.Otherwise return false
    public boolean setStatusToEditText(HttpResponse response){
        boolean isSuccessful = false;
        TextView editText = (TextView)findViewById(R.id.loginPageStatus);
        String status = getStatusCode(response);
        String statusMessage = getStatusMessage(status);
        editText.setText(statusMessage, TextView.BufferType.EDITABLE);
        if(StatusCode.LOGIN_SUCCESSFULLY.equals(status)) {
            isSuccessful = true;
        }
        return isSuccessful;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(MUSIC_RUNNER_MAIN_REQUEST == requestCode && RESULT_OK == resultCode) {
            finish();
        } else if (FACEBOOK_LOGIN_REQUEST == requestCode && RESULT_OK == resultCode) {
            loginSharedPreferences.edit().remove(STATUS).putInt(STATUS, STATUS_LOGIN).commit();
            Intent intent = new Intent(this, MusicRunnerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_skip:
                loginSharedPreferences.edit().putInt(STATUS, STATUS_LOGOUT).commit();
                Intent intent = new Intent(this, MusicRunnerActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
