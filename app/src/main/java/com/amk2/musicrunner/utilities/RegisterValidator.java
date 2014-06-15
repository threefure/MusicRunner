package com.amk2.musicrunner.utilities;

/**
 * Created by paulou1009 on 6/15/14.
 */
public class RegisterValidator {
    public static boolean validatePassword(String password){
        boolean isValid = true;
        if(password == null || password.length() == 0)
            isValid = false;

        return isValid;
    }

    public static  boolean validateAccount(String account){
        boolean isValid = true;
        if(account == null)
            isValid = false;
        else if(account.indexOf(" ") > -1)
            isValid = false;

        return isValid;
    }
}
