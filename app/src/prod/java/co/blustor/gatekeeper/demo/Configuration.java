package co.blustor.gatekeeper.demo;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.authentication.GKCardAuthentication;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeperdemo.Application;

public class Configuration implements Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    @Override
    public GKCardAuthentication getAuthentication() {
        return new GKCardAuthentication(getGKCard());
    }

    @Override
    public GKCard getGKCard() {
        try {
            return GKCardConnector.find();
        } catch (IOException e) {
            Log.e(TAG, "Unable to find GateKeeper Bluetooth Card", e);
            return null;
        }
    }
}
