package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.util.FileUtils;

public class GKFileBrowser {
    public static final String TAG = GKFileBrowser.class.getSimpleName();

    private final GKCard mCard;
    private Stack<String> mCurrentPath = new Stack<>();

    public GKFileBrowser(GKCard card) {
        mCard = card;
    }

    public List<VaultFile> listFiles() throws IOException {
        return mCard.listFiles(getCurrentPath());
    }

    public File getFile(final VaultFile file) throws IOException {
        return mCard.downloadFile(file);
    }

    public boolean putFile(InputStream localFile, String filename) throws IOException {
        String remoteDestinationPath = FileUtils.joinPath(getCurrentPath(), filename);
        return mCard.uploadFile(remoteDestinationPath, localFile);
    }

    public boolean deleteFile(VaultFile file) throws IOException {
        String remoteFileAbsolutePath = FileUtils.joinPath(getCurrentPath(), file.getName());
        if (file.getType() == VaultFile.Type.FILE) {
            return mCard.deleteFile(remoteFileAbsolutePath);
        } else {
            return mCard.removeDirectory(remoteFileAbsolutePath);
        }
    }

    public boolean makeDirectory(String directoryName) throws IOException {
        String fullPath = FileUtils.joinPath(getCurrentPath(), directoryName);
        return mCard.makeDirectory(fullPath);
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
        String rootPath = mCard.getRootPath();
        String subPath = FileUtils.joinPath(mCurrentPath.toArray());
        return FileUtils.joinPath(rootPath, subPath);
    }

    public boolean isAtRoot() {
        return mCurrentPath.empty();
    }
}
