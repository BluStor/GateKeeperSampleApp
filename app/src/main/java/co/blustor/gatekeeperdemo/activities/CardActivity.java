package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.fragments.RequestPairDialogFragment;

public class CardActivity extends ActionBarActivity {
    protected GKCard mCard;

    protected CardState mCardState;

    enum CardState {
        FOUND,
        NOT_FOUND,
        BLUETOOTH_DISABLED,
        BLUETOOTH_UNAVAILABLE
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
}
