package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Set;

public class GKCardConnector {
    public static GKCard findByBluetoothDeviceName(String deviceName) throws IOException {
        BluetoothDevice device = getBluetoothDevice(deviceName);
        return new GKBluetoothCard(device);
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
