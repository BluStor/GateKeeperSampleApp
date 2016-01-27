package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.AuthFragment;

public class AuthActivity extends CardActivity {
    public static final String RESTARTED = "RestartAuthActivity";

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
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_auth;
    }

    @Override
    protected void setInitialFragment() {
        pushFragment(new AuthFragment(), AuthFragment.TAG);
    }
}
