package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.apps.filevault.VaultFile.Type;
import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.bftp.IOMultiplexer;
import co.blustor.gatekeeper.data.GKFile;

public class GKBluetoothCard implements GKCard {
    public final static String TAG = GKBluetoothCard.class.getSimpleName();

    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice mBluetoothDevice;
    private CardClient mClient;
    private BluetoothSocket mSocket;

    public GKBluetoothCard(BluetoothDevice device) throws IOException {
        mBluetoothDevice = device;
    }

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        GKFile[] files = mClient.listFiles(targetPath);
        ArrayList<VaultFile> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                result.add(new FTPVaultFile(targetPath, files[i]));
            }
        }
        return result;
    }

    @Override
    public File downloadFile(VaultFile vaultFile) throws IOException {
        File targetFile = vaultFile.getLocalPath();
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        mClient.retrieveFile(vaultFile.getRemotePath(), outputStream);
        return targetFile;
    }

    @Override
    public boolean uploadFile(String targetPath, InputStream localFile) throws IOException {
        return mClient.storeFile(targetPath, localFile);
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
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
            mSocket.connect();
            InputStream is = mSocket.getInputStream();
            OutputStream os = mSocket.getOutputStream();
            IOMultiplexer multiplexer = new IOMultiplexer(is, os);
            mClient = new CardClient(multiplexer);
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    private class FTPVaultFile extends VaultFile {
        public FTPVaultFile(String targetPath, GKFile file) {
            super(file.getName(), getFileType(file));
            setRemotePath(targetPath, file.getName());
        }
    }

    private Type getFileType(GKFile file) {
        return file.isDirectory() ? Type.DIRECTORY : Type.FILE;
    }
}
