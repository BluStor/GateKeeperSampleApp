package co.blustor.gatekeeper.data;

import java.io.IOException;

public interface IOConnection {
    void open() throws IOException;
    void close() throws IOException;
}
