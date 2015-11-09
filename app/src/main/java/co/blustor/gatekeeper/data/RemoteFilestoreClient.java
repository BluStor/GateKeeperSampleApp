package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RemoteFilestoreClient {
    List<VaultFile> listFiles(String targetPath) throws IOException;
    File downloadFile(VaultFile vaultFile) throws IOException;
    boolean uploadFile(String targetPath, InputStream localFile) throws IOException;
    boolean deleteFile(String fileAbsolutePath) throws IOException;
    boolean removeDirectory(String directoryAbsolutePath) throws IOException;
    String getRootPath();
    void open() throws IOException;
    void close() throws IOException;
}
