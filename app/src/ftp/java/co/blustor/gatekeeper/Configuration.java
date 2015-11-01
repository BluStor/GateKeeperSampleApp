package co.blustor.gatekeeper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import co.blustor.gatekeeper.bluetooth.SerialPortFTPClient;
import co.blustor.gatekeeper.bluetooth.SerialPortMultiplexer;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.LocalFilestore;

import co.blustor.gatekeeper.bluetooth.BluetoothFTPClientOLD;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.net.FTPFilestoreClient;

import static android.os.Environment.getExternalStorageDirectory;

public class Configuration {
    private static final String TAG = "Configuration";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected static File getAppDataPath() {
        String basePath = getExternalStorageDirectory().getAbsolutePath();
        String dataPath = FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        return new File(basePath, dataPath);
    }

    public static File getCachePath() {
        return new File(getAppDataPath(), "_cache");
    }

    public static RemoteFilestore getRemoteFilestore() {
        //RemoteFilestoreClient client = getRemoteFilestoreClient();
        //BluetoothFTPClientOLD ftpClient = new BluetoothFTPClientOLD();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        for(BluetoothDevice d : pairedDevices) {
            if(d.getName().equals("BLUSTOR")) {
                device = d;
            }
        }

        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
            //Log.e(TAG, "Got the socket...");
            socket.connect();
            //Log.e(TAG, "Connected...");

            OutputStream os = socket.getOutputStream();
            //Log.e(TAG, "Got the OutputStream...");

            InputStream is = socket.getInputStream();
            //Log.e(TAG, "Got the InputStream...");

            SerialPortMultiplexer multiplexer = new SerialPortMultiplexer(is, os);
            //Log.e(TAG, "Made the multiplexer");


            SerialPortFTPClient ftpClient = new SerialPortFTPClient(multiplexer);
            FTPFilestoreClient client = new FTPFilestoreClient(ftpClient);
            return new RemoteFilestore(client);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FileVault getFileVault() {
        File cachePath = getCachePath();
        LocalFilestore localFilestore = new LocalFilestore(cachePath);
        RemoteFilestore remoteFilestore = getRemoteFilestore();
        return new FileVault(localFilestore, remoteFilestore);
    }

//    @NonNull
//    private static RemoteFilestoreClient getRemoteFilestoreClient() {
//        return new FTPFilestoreClient();
//    }
}
