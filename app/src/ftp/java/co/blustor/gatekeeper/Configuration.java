package co.blustor.gatekeeper;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.bftp.SerialPortFTPClientFactory;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.LocalFilestore;

import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.FTPFilestoreClient;
import co.blustor.gatekeeper.ftp.FTPClient;

import static android.os.Environment.getExternalStorageDirectory;

public class Configuration {
    private static final String TAG = "Configuration";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    protected static File getAppDataPath() {
        String basePath = getExternalStorageDirectory().getAbsolutePath();
        String dataPath = FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        return new File(basePath, dataPath);
    }

    public static File getCachePath() {
        return new File(getAppDataPath(), "_cache");
    }

    public static RemoteFilestore getRemoteFilestore() {
        try {
            SerialPortFTPClientFactory factory = new SerialPortFTPClientFactory();
            FTPClient ftpClient = factory.createFromPairedBluetoothDevice(PAIRED_BLUETOOTH_DEVICE_NAME);
            //co.blustor.gatekeeper.ftp.FTPClient ftpClient = new ApacheFTPClient();

            FTPFilestoreClient client = new FTPFilestoreClient(ftpClient);
            return new RemoteFilestore(client);
        } catch (IOException e) {
            Log.e(TAG, "Error attempting to create FTPClient.", e);
            return null;
        }
    }

    public static FileVault getFileVault() {
        File cachePath = getCachePath();
        LocalFilestore localFilestore = new LocalFilestore(cachePath);
        RemoteFilestore remoteFilestore = getRemoteFilestore();
        return new FileVault(localFilestore, remoteFilestore);
    }
}
