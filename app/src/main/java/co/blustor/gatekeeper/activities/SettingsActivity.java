package co.blustor.gatekeeper.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.fragments.SettingsFragment;

public class SettingsActivity extends CardActivity {
    public static final String TAG = FileBrowserActivity.class.getSimpleName();

    private SettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setContentFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void setContentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.TAG);

        if (mFragment == null) {
            mFragment = new SettingsFragment();
        }

        fm.beginTransaction()
          .replace(R.id.fragment_container, mFragment, SettingsFragment.TAG)
          .commit();
    }
}
