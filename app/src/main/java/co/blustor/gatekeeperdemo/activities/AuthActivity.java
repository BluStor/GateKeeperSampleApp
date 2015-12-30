package co.blustor.gatekeeperdemo.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.AuthFragment;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.InitializationFragment;

public class AuthActivity extends BaseActivity {
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
        setContentView(R.layout.activity_loading);
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
            setInitialFragment();
        } catch (IOException e) {
            mCardState = CardState.UNABLE_TO_CONNECT;
            Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
        }
    }

    private void setInitialFragment() {
        FragmentManager fm = getSupportFragmentManager();
        CardFragment fragment = (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new AuthFragment();
        }

        fragment.setCard(mCard);

        FragmentTransaction t = fm.beginTransaction();
        t.replace(R.id.fragment_container, fragment, InitializationFragment.TAG);
        t.commit();
    }
}
