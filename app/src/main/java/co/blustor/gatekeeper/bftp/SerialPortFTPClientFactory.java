package co.blustor.gatekeeper.bftp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class SerialPortFTPClientFactory {
    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public SerialPortFTPClient createFromPairedBluetoothDevice(String pairedDeviceName) throws IOException {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName().equals(pairedDeviceName)) {
                device = d;
            }
        }
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
        socket.connect();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        SerialPortMultiplexer multiplexer = new SerialPortMultiplexer(is, os);
        return new SerialPortFTPClient(multiplexer);
    }
}
