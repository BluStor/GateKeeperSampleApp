package co.blustor.gatekeeper.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import co.blustor.gatekeeper.R;

public class ActionBarActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setIcon(R.drawable.ic_blustor_logo_android_toolbar);
    }
}
