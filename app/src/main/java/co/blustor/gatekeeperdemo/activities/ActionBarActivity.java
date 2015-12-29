package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import co.blustor.gatekeeperdemo.R;

public class ActionBarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setIcon(R.drawable.ic_blustor_logo_android_toolbar);
    }
}
