package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.bftp.IOMultiplexer;
import co.blustor.gatekeeper.data.GKFile;

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
    public CardClient.Response retrieve(String cardPath) throws IOException {
        return mClient.retrieve(cardPath);
    }

    @Override
    public CardClient.Response list(String cardPath) throws IOException {
        return mClient.list(cardPath);
    }

    @Override
    public CardClient.Response delete(String cardPath) throws IOException {
        return mClient.delete(cardPath);
    }

    @Override
    public File downloadFile(GKFile cardFile, File localFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(localFile);
        mClient.retrieveFile(cardFile.getCardPath(), outputStream);
        return localFile;
    }

    @Override
    public CardClient.Response store(String targetPath, InputStream inputStream) throws IOException {
        return mClient.store(targetPath, inputStream);
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        return mClient.deleteFile(fileAbsolutePath);
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        return mClient.makeDirectory(directoryAbsolutePath);
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        return mClient.removeDirectory(directoryAbsolutePath);
    }

    @Override
    public String getRootPath() {
        return "/";
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
