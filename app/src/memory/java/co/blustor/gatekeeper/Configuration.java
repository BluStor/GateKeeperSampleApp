package co.blustor.gatekeeper;

import java.io.File;

import co.blustor.gatekeeper.data.AssetsFilestoreClient;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.LocalFilestore;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class Configuration {
    public static RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = new AssetsFilestoreClient();
        return new RemoteFilestore(client);
    }

    public static FileVault getFileVault() {
        File cachePath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        LocalFilestore localFilestore = new LocalFilestore(cachePath);
        RemoteFilestore remoteFilestore = getRemoteFilestore();
        return new FileVault(localFilestore, remoteFilestore);
    }
}
