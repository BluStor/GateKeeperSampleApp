package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.util.FileUtils;

public class GKFileBrowser {
    public static final String TAG = GKFileBrowser.class.getSimpleName();

    private final RemoteFilestoreClient mFilestoreClient;
    private Stack<String> mCurrentPath = new Stack<>();

    public GKFileBrowser(RemoteFilestoreClient client) {
        mFilestoreClient = client;
    }

    public List<VaultFile> listFiles() throws IOException {
        return mFilestoreClient.listFiles(getCurrentPath());
    }

    public File getFile(final VaultFile file) throws IOException {
        return mFilestoreClient.downloadFile(file);
    }

    public boolean putFile(InputStream localFile, String filename) throws IOException {
        String remoteDestinationPath = FileUtils.joinPath(getCurrentPath(), filename);
        return mFilestoreClient.uploadFile(remoteDestinationPath, localFile);
    }

    public boolean deleteFile(VaultFile file) throws IOException {
        String remoteFileAbsolutePath = FileUtils.joinPath(getCurrentPath(), file.getName());
        if (file.getType() == VaultFile.Type.FILE) {
            return mFilestoreClient.deleteFile(remoteFileAbsolutePath);
        } else {
            return mFilestoreClient.removeDirectory(remoteFileAbsolutePath);
        }
    }

    public boolean makeDirectory(String directoryName) throws IOException {
        String fullPath = FileUtils.joinPath(getCurrentPath(), directoryName);
        return mFilestoreClient.makeDirectory(fullPath);
    }

    public void navigateTo(String path) {
        mCurrentPath.push(path);
    }

    public void navigateUp() {
        if (!mCurrentPath.empty()) {
            mCurrentPath.pop();
        }
    }

    private String getCurrentPath() {
        String rootPath = mFilestoreClient.getRootPath();
        String subPath = FileUtils.joinPath(mCurrentPath.toArray());
        return FileUtils.joinPath(rootPath, subPath);
    }

    public boolean isAtRoot() {
        return mCurrentPath.empty();
    }
}
