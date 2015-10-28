package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RemoteFilestoreClient extends IOConnection {
    List<AbstractFile> listFiles(String targetPath) throws IOException;

    File downloadFile(String remotePath, File targetFile) throws IOException;

    String getRootPath();
}
