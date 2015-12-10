package co.blustor.gatekeeper.demo;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.authentication.DemoAuthentication;
import co.blustor.gatekeeper.bftp.SerialPortFTPClientFactory;
import co.blustor.gatekeeper.data.FTPFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.ftp.FTPClient;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    private static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    @Override
    public Authentication getAuthentication() {
        return new DemoAuthentication();
    }

    @Override
    public RemoteFilestore getRemoteFilestore() {
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
