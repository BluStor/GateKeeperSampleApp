package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RemoteFilestoreClient {
    List<VaultFile> listFiles(String targetPath) throws IOException;
    File downloadFile(VaultFile vaultFile) throws IOException;
    String getRootPath();
    void open() throws IOException;
    void close() throws IOException;
}
