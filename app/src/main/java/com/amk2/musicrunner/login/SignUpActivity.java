package com.amk2.musicrunner.login;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.amk2.musicrunner.utilities.RestfulUtility;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daz on 11/9/14.
 */
public class SignUpActivity extends Activity implements View.OnClickListener {
    private ActionBar mActionBar;
    private Button signupButton;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_with_email);
        mActionBar = getActionBar();
        mActionBar.hide();
        initViews();
        setViews();
    }

    private void initViews () {
        signupButton = (Button) findViewById(R.id.email_sign_up);
    }

    private void setViews () {
        signupButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.email_sign_up:
                //intent = new Intent(this, SignUpActivity.class);
                //startActivity(intent);
                EditText emailET = (EditText) findViewById(R.id.email);
                String email = emailET.getText().toString();
                EditText passwordET = (EditText) findViewById(R.id.password);
                String password = passwordET.getText().toString();
                EditText confirmPasswordET = (EditText) findViewById(R.id.confirm_password);
                String confirmPassword = confirmPasswordET.getText().toString();
                EditText firstNameET = (EditText) findViewById(R.id.first_name);
                String firstName = firstNameET.getText().toString();
                EditText lastNAmeET = (EditText) findViewById(R.id.last_name);
                String lastName = lastNAmeET.getText().toString();

                if(validateRegister(email, password, confirmPassword, firstName, lastName)){
                    //registration form is valid, process register
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    pairs.add(new BasicNameValuePair("userAccount", email));
                    pairs.add(new BasicNameValuePair("password",password));
                    HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.REGISTER_ENDPOINT, pairs);
                    //setStatusToEditText(response);
                }
                break;
        }
    }

    private boolean validateRegister(String email, String password, String confirmPassword, String firstName, String lastName){
        boolean valid = true;
        if(password == null || confirmPassword == null || email == null || firstName == null || lastName == null)
            valid = false;
        else if(!password.equals(confirmPassword))
            valid = false;
        return valid;
    }
}
