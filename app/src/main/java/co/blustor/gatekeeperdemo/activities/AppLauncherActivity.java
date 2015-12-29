package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.AppLauncherFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;

public class AppLauncherActivity extends CardActivity {
    public static final String TAG = AppLauncherActivity.class.getSimpleName();

    private AppLauncherFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        setContentFragment();
    }

    @Override
    public void onBackPressed() {
        promptSignOut();
    }

    private void setContentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (AppLauncherFragment) fm.findFragmentByTag(SettingsFragment.TAG);

        if (mFragment == null) {
            mFragment = new AppLauncherFragment();
        }

        mFragment.setCard(mCard);

        fm.beginTransaction()
          .replace(R.id.fragment_container, mFragment, SettingsFragment.TAG)
          .commit();
    }
}
