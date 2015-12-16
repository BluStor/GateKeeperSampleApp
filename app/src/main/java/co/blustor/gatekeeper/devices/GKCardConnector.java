package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Set;

public class GKCardConnector {
    private static final String FIXED_DEVICE_NAME = "BLUSTOR";

    private static GKCard mCard;

    public static GKCard find() throws GKCardNotFound, BluetoothDisabledException, BluetoothUnavailableException {
        return findByBluetoothDeviceName(FIXED_DEVICE_NAME);
    }

    private static GKCard findByBluetoothDeviceName(String deviceName) throws BluetoothDisabledException, BluetoothUnavailableException, GKCardNotFound {
        if (mCard == null) {
            BluetoothAdapter adapter = getBluetoothAdapter();
            BluetoothDevice device = getBluetoothDevice(adapter, deviceName);
            mCard = new GKBluetoothCard(device);
        }
        return mCard;
    }

    private static BluetoothAdapter getBluetoothAdapter() throws BluetoothDisabledException, BluetoothUnavailableException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new BluetoothUnavailableException();
        }
        if (!adapter.isEnabled()) {
            throw new BluetoothDisabledException();
        }
        return adapter;
    }

    @Nullable
    private static BluetoothDevice getBluetoothDevice(BluetoothAdapter adapter, String deviceName) throws GKCardNotFound {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                return device;
            }
        }
        throw new GKCardNotFound(deviceName);
    }

    public static class GKCardNotFound extends IOException {
        public GKCardNotFound(String cardName) {
            super("GateKeeper Card with name '" + cardName + "' not found");
        }
    }

    public static class BluetoothDisabledException extends IOException {
        private static final String MESSAGE = "Bluetooth is disabled";

        public BluetoothDisabledException() {
            super(MESSAGE);
        }
    }

    public static class BluetoothUnavailableException extends IOException {
        private static final String MESSAGE = "Bluetooth is not available on this device";

        public BluetoothUnavailableException() {
            super(MESSAGE);
        }
    }
}
