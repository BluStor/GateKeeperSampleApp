package co.blustor.gatekeeper;

import co.blustor.gatekeeper.data.AssetsFilestoreClient;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.LocalFilestore;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration {
    public static RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = new AssetsFilestoreClient();
        return new RemoteFilestore(client);
    }

    public static FileVault getFileVault() {
        LocalFilestore localFilestore = new LocalFilestore();
        RemoteFilestore remoteFilestore = getRemoteFilestore();
        return new FileVault(localFilestore, remoteFilestore);
    }
}
