package co.blustor.gatekeeper.devices;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.bftp.CardClient.Response;

public interface GKCard {
    Response list(String cardPath) throws IOException;
    Response retrieve(String cardPath) throws IOException;
    Response store(String cardPath, InputStream inputStream) throws IOException;
    Response delete(String cardPath) throws IOException;
    Response makeDirectory(String cardPath) throws IOException;
    Response removeDirectory(String cardPath) throws IOException;
    void connect() throws IOException;
    void disconnect() throws IOException;
}
