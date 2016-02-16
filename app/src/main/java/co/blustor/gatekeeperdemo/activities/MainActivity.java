package co.blustor.gatekeeperdemo.activities;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.filevault.FileVaultFragment;
import co.blustor.gatekeepersdk.devices.GKCard;

public class MainActivity extends CardActivity {
    private Menu mMenu;

    @Override
    protected void setInitialFragment() {
        pushFragment(new FileVaultFragment(), FileVaultFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSupportActionBar().isShowing()) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_general, menu);
            mMenu = menu;
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
        updateMenu(state);
        boolean isConnected = state.equals(GKCard.ConnectionState.CONNECTED);
        boolean isTransferring = state.equals(GKCard.ConnectionState.TRANSFERRING);
        if (isConnected || isTransferring) {
            super.updateConnectionStateUI(state);
        } else {
            restartAuthActivity();
        }
    }

    private void updateMenu(GKCard.ConnectionState state) {
        if (mMenu != null) {
            if (state.equals(GKCard.ConnectionState.TRANSFERRING)) {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
            } else if (state.equals(GKCard.ConnectionState.CONNECTED)) {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_white_24dp);
            } else {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_disabled_white_24dp);
            }
        }
    }
}
