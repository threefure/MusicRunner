package com.amk2.musicrunner.login;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.amk2.musicrunner.utilities.LoginUtils;
import com.amk2.musicrunner.utilities.RestfulUtility;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by daz on 11/9/14.
 */
public class SignUpActivity extends Activity implements View.OnClickListener {
    private ActionBar mActionBar;
    private Button signupButton;
    public static final String LOGIN = "login";
    public static final String STATUS = "status";
    public static final String USER_NAME  = "user_name";
    public static final int STATUS_NONE   = 0;
    public static final int STATUS_LOGIN  = 1;
    public static final int STATUS_LOGOUT = 2;
    private ProgressDialog progressDialog;
    private Activity self;
    private SharedPreferences loginSharedPreferences;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_with_email);
        mActionBar = getActionBar();
        self = this;
        loginSharedPreferences = getSharedPreferences(LOGIN, MODE_PRIVATE);
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
                progressDialog = ProgressDialog.show(self, "Register...", getString(R.string.please_wait));
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
                    pairs.add(new BasicNameValuePair("firstName",firstName));
                    pairs.add(new BasicNameValuePair("lastName",lastName));
                    String fullName = firstName + " " + lastName;
                    HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.REGISTER_ENDPOINT, pairs);
                    String statusString = LoginUtils.getStatusString(response);
                    nextStep(statusString,fullName);
                }
                break;
        }
    }

    private void nextStep(String status, String fullName){
        if(status == null){
            //do nothing
        } else if (status.equals(LoginUtils.DUPLICATE_ACCOUNT))
            showDuplicateUserDialog();
        else if (status.equals(LoginUtils.REGISTER_FAIL))
            showRegisterFailDialog();
        else if (status.equals(LoginUtils.REGISTER_SUCCESSFULLY)){
            //redirect to run page
            loginSharedPreferences.edit().remove(USER_NAME).putString(USER_NAME, fullName).commit();
            loginSharedPreferences.edit().remove(STATUS).putInt(STATUS, STATUS_LOGIN).commit();
            Intent intent = new Intent(this, MusicRunnerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            showConnectionErrorDialog();
        }
    }

    private boolean validateRegister(String email, String password, String confirmPassword, String firstName, String lastName){
        boolean valid = true;
        if(password == null || confirmPassword == null || email == null || firstName == null || lastName == null){
            valid = false;
            showNoEmptyField();
        } else if(!password.equals(confirmPassword)) {
            valid = false;
            showPasswordNotSameDialog();
        } else if(!validateEmailPattern(email)){
            valid = false;
            showInvalidEmailFormatDialog();
        }
        return valid;
    }

    public boolean validateEmailPattern(String email){
        if(email == null)
            return false;

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);;
        Matcher matcher = pattern.matcher(email);;

        return matcher.matches();
    }

    private void showInvalidEmailFormatDialog(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("You need to enter valid email")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }

    private void showConnectionErrorDialog(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("Opps...Server does not respond. Please check your internet connection and try again later.")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }
    private void showRegisterFailDialog(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("Sorry, register has failed. Please try again later")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }

    private void showDuplicateUserDialog(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("This email has been registered, please use another email")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }

    private void showNoEmptyField(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("Please make sure you enter all fields")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }

    private void showPasswordNotSameDialog(){
        progressDialog.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("Please make sure you enter the same password for 'Confirm Password'")
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        dialog.show();
    }
}
