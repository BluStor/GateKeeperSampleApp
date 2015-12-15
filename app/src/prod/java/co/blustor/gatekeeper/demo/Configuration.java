package co.blustor.gatekeeper.demo;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    private static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    @Override
    public Authentication getAuthentication() {
        return new DemoAuthentication();
    }

    @Override
    public GKCard getGKCard() {
        try {
            return GKCardConnector.findByBluetoothDeviceName(PAIRED_BLUETOOTH_DEVICE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Unable to find GateKeeper Bluetooth Card", e);
            return null;
        }
    }
}
