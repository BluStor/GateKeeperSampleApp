package co.blustor.gatekeeper.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.fragments.FileBrowserFragment;

public class FileBrowserActivity extends CardActivity {
    public static final String TAG = FileBrowserActivity.class.getSimpleName();

    private FileBrowserFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        setContentFragment();
    }

    @Override
    public void onBackPressed() {
        if (mFragment.canNavigateBack()) {
            mFragment.navigateBack();
        } else {
            super.onBackPressed();
        }
    }

    private void setContentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (FileBrowserFragment) fm.findFragmentByTag(FileBrowserFragment.TAG);

        if (mFragment == null) {
            mFragment = new FileBrowserFragment();
        }

        fm.beginTransaction()
          .replace(R.id.fragment_container, mFragment, FileBrowserFragment.TAG)
          .commit();
    }
}
