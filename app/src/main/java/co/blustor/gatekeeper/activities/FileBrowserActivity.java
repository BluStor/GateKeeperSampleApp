package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.fragments.FileBrowserFragment;

public class FileBrowserActivity extends Activity {
    private FileBrowserFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        FragmentManager fm = getFragmentManager();
        mFragment = (FileBrowserFragment) fm.findFragmentByTag(FileBrowserFragment.TAG);

        if (mFragment == null) {
            mFragment = new FileBrowserFragment();
            fm.beginTransaction().add(R.id.fragment_container, mFragment, FileBrowserFragment.TAG).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragment.canNavigateBack()) {
            mFragment.navigateBack();
        } else {
            super.onBackPressed();
        }
    }
}
