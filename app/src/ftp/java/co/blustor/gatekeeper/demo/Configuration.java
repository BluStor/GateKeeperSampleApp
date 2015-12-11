package co.blustor.gatekeeper.demo;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.data.FTPFilestoreClient;
import co.blustor.gatekeeper.data.GKFileBrowser;

public class Configuration implements co.blustor.gatekeeper.demo.Application.Configuration {
    private static final String TAG = Configuration.class.getSimpleName();

    private static final String PAIRED_BLUETOOTH_DEVICE_NAME = "BLUSTOR";

    @Override
    public Authentication getAuthentication() {
        return new DemoAuthentication();
    }

    @Override
    public GKFileBrowser getRemoteFilestore() {
        try {
            FTPFilestoreClient client = new FTPFilestoreClient(PAIRED_BLUETOOTH_DEVICE_NAME);
            return new GKFileBrowser(client);
        } catch (IOException e) {
            Log.e(TAG, "Error attempting to create GKFileBrowser.", e);
            return null;
        }
    }
}
