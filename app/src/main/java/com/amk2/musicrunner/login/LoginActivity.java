package com.amk2.musicrunner.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.constants.SharedPreferenceConstants;
import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.amk2.musicrunner.constants.StatusCode;
import com.amk2.musicrunner.utilities.RegisterValidator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
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
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://ec2-54-187-71-254.us-west-2.compute.amazonaws.com:8080/register");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("userAccount", account));
            pairs.add(new BasicNameValuePair("password",password));
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = client.execute(post);
                setStatusToEditText(response);
            } catch (UnsupportedEncodingException uee) {

            } catch (ClientProtocolException cpe) {
                //ignore this exception for now
            } catch (IOException ioe) {
                //ignore this exception for now
            }
        }
    }

    public void loginAccount(View view) {
        EditText accountEditText = (EditText) findViewById(R.id.account_info);
        String account = accountEditText.getText().toString().trim();

        EditText passwordEditText = (EditText) findViewById(R.id.password);
        String password = passwordEditText.getText().toString();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://ec2-54-187-71-254.us-west-2.compute.amazonaws.com:8080/login");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("userAccount", account));
        pairs.add(new BasicNameValuePair("password",password));
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            HttpResponse response = client.execute(post);
            boolean isSuccessful = setStatusToEditText(response);

            if(isSuccessful){
                SharedPreferences preferences = getSharedPreferences(SharedPreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
                preferences.edit().putString(SharedPreferenceConstants.ACCOUNT_PARAMS, account).commit();
                Intent intent = new Intent(this, MusicRunnerActivity.class);
                startActivity(intent);
            }


        } catch (UnsupportedEncodingException uee) {

        } catch (ClientProtocolException cpe) {
            //ignore this exception for now
        } catch (IOException ioe) {
            //ignore this exception for now
        }
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
        } else if (StatusCode.WRONG_PASSWORD.equals(statusCode)){
            return "Password is not correct";
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
}
