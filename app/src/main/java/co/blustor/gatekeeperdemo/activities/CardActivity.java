package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.filevault.FileVaultFragment;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.RequestPairDialogFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;
import co.blustor.gatekeeperdemo.fragments.TestsFragment;

public class CardActivity extends ActionBarActivity {
    protected GKCard mCard;

    protected CardState mCardState;

    enum CardState {
        FOUND,
        NOT_FOUND,
        BLUETOOTH_DISABLED,
        BLUETOOTH_UNAVAILABLE,
        UNABLE_TO_CONNECT
    }

    public void openSettings() {
        pushFragment(new SettingsFragment(), SettingsFragment.TAG);
    }

    public void openTests() {
        pushFragment(new TestsFragment(), TestsFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openSettings();
                return true;
            case R.id.sign_out:
                promptSignOut();
                return true;
            case R.id.tests:
                openTests();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        CardFragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment.canNavigateBack()) {
            currentFragment.navigateBack();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            promptSignOut();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        try {
            mCard = GKCardConnector.find();
            mCardState = CardState.FOUND;
        } catch (GKCardConnector.GKCardNotFound e) {
            mCardState = CardState.NOT_FOUND;
        } catch (GKCardConnector.BluetoothDisabledException e) {
            mCardState = CardState.BLUETOOTH_DISABLED;
        } catch (GKCardConnector.BluetoothUnavailableException e) {
            mCardState = CardState.BLUETOOTH_UNAVAILABLE;
        }
        try {
            mCard.connect();
            if (!hasFragment()) {
                setInitialFragment();
            }
        } catch (IOException e) {
            mCardState = CardState.UNABLE_TO_CONNECT;
            Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
        }
    }

    protected void setInitialFragment() {
        pushFragment(new FileVaultFragment(), "appLauncher");
    }

    protected void pushFragment(CardFragment fragment, String tag) {
        fragment.setCard(mCard);
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mCardState == CardState.NOT_FOUND) {
            RequestPairDialogFragment dialog = new RequestPairDialogFragment();
            dialog.show(getSupportFragmentManager(), "requestPairWithCard");
        } else if (mCardState != CardState.FOUND) {
            // No Bluetooth; Cannot continue
        } else {
            // Found; Continue
        }
    }

    protected void promptSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sign_out_confirm);
        builder.setPositiveButton(R.string.sign_out_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSignOut();
            }
        });
        builder.setNegativeButton(R.string.sign_out_no, null);
        builder.create().show();
    }

    protected void onSignOut() {
        final Activity activity = this;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
                    GKAuthentication auth = new GKAuthentication(mCard);
                    auth.signOut();
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.finishAffinity();
            }
        }.execute();
    }

    private CardFragment getCurrentFragment() {
        return (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
}
