package co.blustor.gatekeeper;

import co.blustor.gatekeeper.data.AssetsFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration {
    public static RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = new AssetsFilestoreClient();
        return new RemoteFilestore(client);
    }
}
