package co.blustor.gatekeeper.devices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.apps.filevault.VaultFile;

public interface GKCard {
    List<VaultFile> listFiles(String targetPath) throws IOException;
    File downloadFile(VaultFile vaultFile) throws IOException;
    boolean uploadFile(String targetPath, InputStream localFile) throws IOException;
    boolean deleteFile(String fileAbsolutePath) throws IOException;
    boolean removeDirectory(String directoryAbsolutePath) throws IOException;
    boolean makeDirectory(String directoryAbsolutePath) throws IOException;
    String getRootPath();
}
