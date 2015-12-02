package co.blustor.gatekeeper;

import co.blustor.gatekeeper.data.AndroidFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration {
    public static RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = new AndroidFilestoreClient();
        return new RemoteFilestore(client);
    }
}
