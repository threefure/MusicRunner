package com.amk2.musicrunner.login;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import com.amk2.musicrunner.R;

/**
 * Created by daz on 11/9/14.
 */
public class SignUpActivity extends Activity {
    private ActionBar mActionBar;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_with_email);
        mActionBar = getActionBar();
        mActionBar.hide();
    }
}
