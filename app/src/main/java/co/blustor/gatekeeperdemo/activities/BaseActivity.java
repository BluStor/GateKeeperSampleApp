package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.CardFragment;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        ActionBar bar = getSupportActionBar();
        if (bar != null && bar.isShowing()) {
            bar.setDisplayUseLogoEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setIcon(R.drawable.ic_blustor_logo_android_toolbar);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!hasFragment()) {
            setInitialFragment();
        }
    }

    protected int getLayoutResource() {
        return R.layout.activity_single_fragment;
    }

    protected abstract void setInitialFragment();

    protected void pushFragment(CardFragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        if (hasFragment()) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    protected boolean hasFragment() {
        return getCurrentFragment() != null;
    }

    protected CardFragment getCurrentFragment() {
        return (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    protected boolean isAtRootFragment() {
        return getSupportFragmentManager().getBackStackEntryCount() == 0;
    }

    protected boolean canHandleBackPressed(CardFragment currentFragment) {
        return currentFragment != null && currentFragment.canNavigateBack();
    }
}
