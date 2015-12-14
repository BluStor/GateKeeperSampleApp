package co.blustor.gatekeeper.activities;

import android.os.Bundle;

import co.blustor.gatekeeper.devices.GKAndroidClient;
import co.blustor.gatekeeper.fragments.RequestPairDialogFragment;

public class CardActivity extends ActionBarActivity {
    protected GKAndroidClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new GKAndroidClient();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mClient.initialize();
        if (!mClient.isPairedWithCard()) {
            RequestPairDialogFragment dialog = new RequestPairDialogFragment();
            dialog.show(getSupportFragmentManager(), "requestPairWithCard");
        }
    }
}
