package com.amk2.musicrunner.utilities;

import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.constants.StatusCode;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by paulou on 11/10/14.
 */
public class LoginUtils {
    public static final String NO_SUCH_USER =  "No Such User";
    public static final String NO_INFO_FROM_SERVER = "Cannot get information from Server, please check your internet connection and try again later.";
    public static final String INCORRECT_PASSWORD =  "Password is not correct";
    public static final String REGISTER_SUCCESSFULLY =  "Register Successfully";
    public static final String REGISTER_FAIL =  "Register fails";
    public static final String FAIL_TO_LOGIN = "Fail to Login";
    public static final String DUPLICATE_ACCOUNT = "Account has already been used, please choose another account name";
    public static final String LOGIN_SUCCESSFULLY = "Login Successfully";

    //return true if users successfully login/register.Otherwise return false
    public static String getStatusString(HttpResponse response){
        String status = getStatusCode(response);
        String statusMessage = getStatusMessage(status);
        return statusMessage;
    }

    public static String getStatusCode(HttpResponse response){
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

    public static String getStatusMessage(String statusCode) {
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
}
