package co.blustor.gatekeeper.devices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.data.GKFile;

public interface GKCard {
    List<GKFile> listFiles(String cardPath) throws IOException;
    File downloadFile(GKFile cardFile, File localFile) throws IOException;
    boolean uploadFile(String cardPath, InputStream localFile) throws IOException;
    boolean deleteFile(String cardPath) throws IOException;
    boolean removeDirectory(String cardPath) throws IOException;
    boolean makeDirectory(String cardPath) throws IOException;
    String getRootPath();
    void connect() throws IOException;
    void disconnect() throws IOException;
}
