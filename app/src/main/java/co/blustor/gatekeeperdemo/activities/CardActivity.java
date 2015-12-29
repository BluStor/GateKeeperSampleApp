package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;

import co.blustor.gatekeeper.scopes.GKCardAuthentication;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeperdemo.fragments.RequestPairDialogFragment;

public class CardActivity extends ActionBarActivity {
    protected GKCard mCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            mCard = GKCardConnector.find();
        } catch (GKCardConnector.GKCardNotFound e) {
            RequestPairDialogFragment dialog = new RequestPairDialogFragment();
            dialog.show(getSupportFragmentManager(), "requestPairWithCard");
        } catch (GKCardConnector.BluetoothDisabledException e) {
        } catch (GKCardConnector.BluetoothUnavailableException e) {
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
                    GKCardAuthentication auth = new GKCardAuthentication(mCard);
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
