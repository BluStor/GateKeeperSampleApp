package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RemoteFilestoreClient extends IOConnection {
    List<VaultFile> listFiles(String targetPath) throws IOException;
    File downloadFile(VaultFile vaultFile, File targetFile) throws IOException;
    String getRootPath();
}
