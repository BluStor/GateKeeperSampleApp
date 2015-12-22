package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.bftp.CardClient.Response;
import co.blustor.gatekeeper.bftp.IOMultiplexer;

public class GKBluetoothCard implements GKCard {
    public final static String TAG = GKBluetoothCard.class.getSimpleName();

    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice mBluetoothDevice;
    private CardClient mClient;
    private BluetoothSocket mSocket;

    public GKBluetoothCard(BluetoothDevice device) {
        mBluetoothDevice = device;
    }

    @Override
    public Response retrieve(String cardPath) throws IOException {
        return mClient.retrieve(cardPath);
    }

    @Override
    public Response list(String cardPath) throws IOException {
        return mClient.list(cardPath);
    }

    @Override
    public Response delete(String cardPath) throws IOException {
        return mClient.delete(cardPath);
    }

    @Override
    public Response store(String cardPath, InputStream inputStream) throws IOException {
        return mClient.store(cardPath, inputStream);
    }

    @Override
    public Response makeDirectory(String cardPath) throws IOException {
        return mClient.makeDirectory(cardPath);
    }

    @Override
    public Response removeDirectory(String cardPath) throws IOException {
        return mClient.removeDirectory(cardPath);
    }

    @Override
    public void connect() throws IOException {
        if (mSocket == null) {
            try {
                mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
                mSocket.connect();
                InputStream is = mSocket.getInputStream();
                OutputStream os = mSocket.getOutputStream();
                IOMultiplexer multiplexer = new IOMultiplexer(is, os);
                mClient = new CardClient(multiplexer);
            } catch (IOException e) {
                mSocket = null;
                mClient = null;
                throw e;
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            if (mClient != null) {
                mClient.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } finally {
            mSocket = null;
            mClient = null;
        }
    }
}
