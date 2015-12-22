package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import co.blustor.gatekeeper.bftp.CardClient;

public class GKBluetoothCard implements GKCard {
    public final static String TAG = GKBluetoothCard.class.getSimpleName();

    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice mBluetoothDevice;
    private CardClient mClient;

    public GKBluetoothCard(BluetoothDevice device) {
        mBluetoothDevice = device;
    }

    @Override
    public Response list(String cardPath) throws IOException {
        cardPath = globularPath(cardPath);
        return mClient.get(CardClient.LIST, cardPath);
    }

    @Override
    public Response get(String cardPath) throws IOException {
        return mClient.get(CardClient.RETR, cardPath);
    }

    @Override
    public Response put(String cardPath, InputStream inputStream) throws IOException {
        return mClient.put(cardPath, inputStream);
    }

    @Override
    public Response delete(String cardPath) throws IOException {
        return mClient.call(CardClient.DELE, cardPath);
    }

    @Override
    public Response createPath(String cardPath) throws IOException {
        return mClient.call(CardClient.MKD, cardPath);
    }

    @Override
    public Response deletePath(String cardPath) throws IOException {
        return mClient.call(CardClient.RMD, cardPath);
    }

    @Override
    public void connect() throws IOException {
        if (mClient == null) {
            try {
                BluetoothSocket socket = mBluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
                mClient = new CardClient(socket);
                mClient.connect();
            } catch (IOException e) {
                mClient = null;
                throw e;
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (mClient != null) {
            try {
                mClient.disconnect();
            } finally {
                mClient = null;
            }
        }
    }

    @NonNull
    private String globularPath(String cardPath) {
        if (cardPath.equals("/")) {
            cardPath += "*";
        } else {
            cardPath += "/*";
        }
        return cardPath;
    }
}
