package co.blustor.gatekeeper.devices;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.bftp.CardClient;

public interface GKCard {
    CardClient.Response list(String cardPath) throws IOException;
    CardClient.Response retrieve(String cardPath) throws IOException;
    CardClient.Response store(String cardPath, InputStream inputStream) throws IOException;
    CardClient.Response delete(String cardPath) throws IOException;
    CardClient.Response makeDirectory(String cardPath) throws IOException;
    boolean removeDirectory(String cardPath) throws IOException;
    void connect() throws IOException;
    void disconnect() throws IOException;
}
