package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.filevault.FileVaultFragment;

public class FileBrowserActivity extends CardActivity {
    public static final String TAG = FileBrowserActivity.class.getSimpleName();

    private FileVaultFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
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
        mFragment = (FileVaultFragment) fm.findFragmentByTag(FileVaultFragment.TAG);

        if (mFragment == null) {
            mFragment = new FileVaultFragment();
        }

        mFragment.setCard(mCard);

        fm.beginTransaction()
          .replace(R.id.fragment_container, mFragment, FileVaultFragment.TAG)
          .commit();
    }
}
