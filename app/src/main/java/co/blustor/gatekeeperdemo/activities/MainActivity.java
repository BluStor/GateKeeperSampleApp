package co.blustor.gatekeeperdemo.activities;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.filevault.FileVaultFragment;

public class MainActivity extends CardActivity {
    @Override
    protected void setInitialFragment() {
        pushFragment(new FileVaultFragment(), FileVaultFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSupportActionBar().isShowing()) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_general, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openSettings();
                return true;
            case R.id.tests:
                openTests();
                return true;
            case R.id.sign_out:
                promptSignOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void updateConnectionStateUI(GKCard.ConnectionState state) {
        if (!state.equals(GKCard.ConnectionState.CONNECTED)) {
            restartAuthActivity();
        } else {
            super.updateConnectionStateUI(state);
        }
    }
}
