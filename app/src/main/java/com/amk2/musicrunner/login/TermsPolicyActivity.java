package com.amk2.musicrunner.login;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.amk2.musicrunner.R;

public class TermsPolicyActivity extends Activity {

    private WebView mTermsPolicyWebView;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_policy_activity);
        initialize();
    }

    private void initialize() {
        findViews();
        initActionBar();
        initTermsPolicyWebView();
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
    }

    private void initTermsPolicyWebView() {
        mTermsPolicyWebView.loadUrl(getString(R.string.terms_policy_content_uri));
    }

    private void findViews() {
        mTermsPolicyWebView = (WebView)findViewById(R.id.terms_policy_content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.terms_policy, menu);
        return false;
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
