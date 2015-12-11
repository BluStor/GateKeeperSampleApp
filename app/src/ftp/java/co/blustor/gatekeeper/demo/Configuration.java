package co.blustor.gatekeeper.demo;

import java.io.IOException;

import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.data.CardFilestoreClient;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    private static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    @Override
    public Authentication getAuthentication() {
        return new DemoAuthentication();
    }

    @Override
    public RemoteFilestoreClient getRemoteFilestoreClient() throws IOException {
        return new CardFilestoreClient(PAIRED_BLUETOOTH_DEVICE_NAME);
    }
}
