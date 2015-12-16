package co.blustor.gatekeeper.activities;

import android.os.Bundle;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeper.fragments.RequestPairDialogFragment;

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
}
