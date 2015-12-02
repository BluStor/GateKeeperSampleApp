package co.blustor.gatekeeper.demo;

import co.blustor.gatekeeper.data.AndroidFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    @Override
    public RemoteFilestore getRemoteFilestore() {
        RemoteFilestoreClient client = new AndroidFilestoreClient();
        return new RemoteFilestore(client);
    }
}
