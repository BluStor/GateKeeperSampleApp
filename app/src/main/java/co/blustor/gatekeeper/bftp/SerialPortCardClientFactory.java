package co.blustor.gatekeeper.bftp;

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

public class SerialPortCardClientFactory {
    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public SerialPortCardClient createFromPairedBluetoothDevice(String pairedDeviceName) throws IOException {
        BluetoothDevice device = getBluetoothDevice(pairedDeviceName);
        SerialPortMultiplexer multiplexer = createMultiplexer(device);
        return new SerialPortCardClient(multiplexer);
    }

    @Nullable
    private BluetoothDevice getBluetoothDevice(String pairedDeviceName) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(pairedDeviceName)) {
                return device;
            }
        }
        return null;
    }

    @NonNull
    private SerialPortMultiplexer createMultiplexer(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
        socket.connect();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        return new SerialPortMultiplexer(is, os);
    }
}
