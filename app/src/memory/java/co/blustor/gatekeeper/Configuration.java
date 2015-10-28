package co.blustor.gatekeeper;

import android.support.annotation.NonNull;

import java.io.File;

import co.blustor.gatekeeper.data.AssetsFilestoreClient;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.LocalFilestore;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

import static android.os.Environment.getExternalStorageDirectory;

public class Configuration {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    protected static File getAppDataPath() {
        String basePath = getExternalStorageDirectory().getAbsolutePath();
        String dataPath = FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        return new File(basePath, dataPath);
    }

    public static File getCachePath() {
        return new File(getAppDataPath(), "_cache");
    }

    public static RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = getRemoteFilestoreClient();
        return new RemoteFilestore(client);
    }

    public static FileVault getFileVault() {
        LocalFilestore localFilestore = new LocalFilestore(getCachePath());
        RemoteFilestore remoteFilestore = getRemoteFilestore();
        return new FileVault(localFilestore, remoteFilestore);
    }

    @NonNull
    private static RemoteFilestoreClient getRemoteFilestoreClient() {
        return new AssetsFilestoreClient();
    }
}
