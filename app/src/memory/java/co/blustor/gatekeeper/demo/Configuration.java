package co.blustor.gatekeeper.demo;

import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.data.AndroidFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    @Override
    public Authentication getAuthentication() {
        return new DemoAuthentication();
    }

    @Override
    public RemoteFilestoreClient getRemoteFilestoreClient() {
        return new AndroidFilestoreClient();
    }
}
