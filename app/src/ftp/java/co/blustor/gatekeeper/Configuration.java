package co.blustor.gatekeeper;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.bftp.SerialPortFTPClientFactory;
import co.blustor.gatekeeper.data.FTPFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.ftp.FTPClient;

public class Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    public static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    public static RemoteFilestore getRemoteFilestore() {
        try {
            SerialPortFTPClientFactory factory = new SerialPortFTPClientFactory();
            FTPClient ftpClient = factory.createFromPairedBluetoothDevice(PAIRED_BLUETOOTH_DEVICE_NAME);
            FTPFilestoreClient client = new FTPFilestoreClient(ftpClient);
            return new RemoteFilestore(client);
        } catch (IOException e) {
            Log.e(TAG, "Error attempting to create FTPClient.", e);
            return null;
        }
    }
}
