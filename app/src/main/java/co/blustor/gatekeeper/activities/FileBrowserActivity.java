package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.fragments.FileBrowserFragment;

public class FileBrowserActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new FileBrowserFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
