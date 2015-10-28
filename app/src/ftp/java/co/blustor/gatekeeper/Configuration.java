package co.blustor.gatekeeper;

import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.net.FTPFilestoreClient;

public class Configuration {
    public static RemoteFilestore getRemoteFilestore() {
        FTPFilestoreClient client = new FTPFilestoreClient();
        return new RemoteFilestore(client);
    }
}
