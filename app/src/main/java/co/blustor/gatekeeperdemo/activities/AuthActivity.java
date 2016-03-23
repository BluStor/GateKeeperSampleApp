package co.blustor.gatekeeperdemo.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.AuthFragment;

public class AuthActivity extends CardActivity {
    public static final String RESTARTED = "RestartAuthActivity";
    private TextView mVersionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        if (bar != null && bar.isShowing()) {
            bar.setDisplayUseLogoEnabled(false);
            bar.setDisplayShowHomeEnabled(false);
        }

        boolean restarted = getIntent().getBooleanExtra(RESTARTED, false);
        mConnectAutomatically = mConnectAutomatically && !restarted;
        mVersionText = (TextView) findViewById(R.id.app_version);
        mVersionText.setText(getVersionText());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_auth;
    }

    @Override
    protected void setInitialFragment() {
        pushFragment(new AuthFragment(), AuthFragment.TAG);
    }

    private String getVersionText() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Could not fetch version";
        }
    }
}
