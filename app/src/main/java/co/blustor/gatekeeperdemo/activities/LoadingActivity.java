package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.InitializationFragment;

public class LoadingActivity extends BaseActivity {
    public static final String TAG = LoadingActivity.class.getSimpleName();

    private InitializationFragment mInitializationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        setInitializationFragment();
    }

    @Override
    public void onBackPressed() {
        mInitializationFragment.cancel();
        super.onBackPressed();
    }

    private void setInitializationFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mInitializationFragment = (InitializationFragment) fm.findFragmentByTag(InitializationFragment.TAG);

        if (mInitializationFragment == null) {
            mInitializationFragment = new InitializationFragment();
        }

        FragmentTransaction t = fm.beginTransaction();
        t.replace(R.id.fragment_container, mInitializationFragment, InitializationFragment.TAG);
        t.commit();
    }
}
