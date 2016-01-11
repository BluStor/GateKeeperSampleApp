package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.RequestPairDialogFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;
import co.blustor.gatekeeperdemo.fragments.TestsFragment;

public abstract class CardActivity extends BaseActivity {
    protected GKCard mCard;
    protected CardState mCardState;

    enum CardState {
        FOUND,
        NOT_FOUND,
        BLUETOOTH_DISABLED,
        BLUETOOTH_UNAVAILABLE,
        UNABLE_TO_CONNECT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        } catch (IOException e) {
            mCardState = CardState.UNABLE_TO_CONNECT;
            Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onBackPressed() {
        CardFragment currentFragment = getCurrentFragment();
        if (canHandleBackPressed(currentFragment)) {
            currentFragment.navigateBack();
        } else if (isAtRootFragment() && isTaskRoot()) {
            promptSignOut();
        } else {
            super.onBackPressed();
        }
    }

    protected void openSettings() {
        pushFragment(new SettingsFragment(), SettingsFragment.TAG);
    }

    protected void openTests() {
        pushFragment(new TestsFragment(), TestsFragment.TAG);
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

    @Override
    protected void pushFragment(CardFragment fragment, String tag) {
        fragment.setCard(mCard);
        super.pushFragment(fragment, tag);
    }
}
