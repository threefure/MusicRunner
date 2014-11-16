package com.amk2.musicrunner.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.SharedPreferencesUtility;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FBLogin extends Activity {
    String facebookUserId = "";
    SharedPreferences preferences;
    Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fblogin);

        preferences = SharedPreferencesUtility.getSharedPreferences(this);
        //facebook login
        Session.openActiveSession(this, true, new Session.StatusCallback() {
            // callback when session changes state

            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    // make request to the /me API
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                TextView welcome = (TextView) findViewById(R.id.facebook_login_status);
                                facebookUserId = Constant.FACEBOOK_ACCOUNT_PREFIX + user.getId();
                                SharedPreferencesUtility.storeAccount(preferences, facebookUserId);
                                String firstName = "";
                                String lastName = "";
                                try {
                                    firstName = URLEncoder.encode(user.getFirstName(), "utf8");
                                    lastName = URLEncoder.encode(user.getLastName(), "utf8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String gender = user.getProperty("gender").toString();
                                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                                pairs.add(new BasicNameValuePair("userAccount", facebookUserId));
                                pairs.add(new BasicNameValuePair("firstName", firstName));
                                pairs.add(new BasicNameValuePair("lastName", lastName));
                                pairs.add(new BasicNameValuePair("gender", gender));

                                HttpResponse httpResponse = RestfulUtility.restfulPostRequest(RestfulUtility.FACEBOOK_LOGIN, pairs);

                                //Intent intent = new Intent(thisActivity, MusicRunnerActivity.class);
                                //thisActivity.startActivity(intent);
                                String userName = user.getName();
                                if (userName == null) {
                                    userName = "";
                                }
                                fbLoginCompleted(user.getId(), userName);
                            }
                        }
                    }).executeAsync();

                }
            }
        });
    }

    private void fbLoginCompleted (String userId, String userName) {
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.USER_NAME, userName);
        intent.putExtra(LoginActivity.USER_ID, userId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fblogin, menu);
        return true;
    }

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
}
