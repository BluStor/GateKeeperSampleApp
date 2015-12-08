package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class GKAndroidClient {
    private final BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice mCardDevice;

    public GKAndroidClient() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void initialize() {
        identifyCard();
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isPairedWithCard() {
        return mCardDevice != null;
    }

    private void identifyCard() {
        if (!isBluetoothEnabled()) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("BLUSTOR")) {
                mCardDevice = device;
                return;
            }
        }
    }
}
