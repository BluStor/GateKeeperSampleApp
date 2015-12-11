package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.bftp.SerialPortMultiplexer;

public class GKCardConnector {
    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static GKCard findByBluetoothDeviceName(String deviceName) throws IOException {
        BluetoothDevice device = getBluetoothDevice(deviceName);
        SerialPortMultiplexer multiplexer = createMultiplexer(device);
        CardClient client = new CardClient(multiplexer);
        return new GKBluetoothCard(client);
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

    @NonNull
    private static SerialPortMultiplexer createMultiplexer(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
        socket.connect();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        return new SerialPortMultiplexer(is, os);
    }
}
