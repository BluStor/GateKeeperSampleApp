package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class GKCardConnector {
    private static final HashMap<String, GKCard> mCards = new HashMap<>();

    public static GKCard findByBluetoothDeviceName(String deviceName) throws IOException {
        GKCard gkCard = mCards.get(deviceName);
        if (gkCard == null) {
            BluetoothDevice device = getBluetoothDevice(deviceName);
            gkCard = new GKBluetoothCard(device);
            mCards.put(deviceName, gkCard);
        }
        return gkCard;
    }

    private static BluetoothAdapter getBluetoothAdapter() throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Bluetooth is not available on this device");
        }
        if (!adapter.isEnabled()) {
            throw new IOException("Bluetooth is disabled");
        }
        return adapter;
    }

    @Nullable
    private static BluetoothDevice getBluetoothDevice(String deviceName) throws IOException {
        BluetoothAdapter mBluetoothAdapter = getBluetoothAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                return device;
            }
        }
        throw new IOException("Bluetooth Device with name '" + deviceName + "' not found");
    }
}
