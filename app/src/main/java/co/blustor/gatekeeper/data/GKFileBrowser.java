package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.devices.GKCard;

public class GKFileBrowser {
    public static final String TAG = GKFileBrowser.class.getSimpleName();

    private final GKCard mCard;

    public GKFileBrowser(GKCard card) {
        mCard = card;
    }

    public List<VaultFile> listFiles(String remotePath) throws IOException {
        return mCard.listFiles(remotePath);
    }

    public File getFile(final VaultFile file) throws IOException {
        return mCard.downloadFile(file);
    }

    public boolean putFile(InputStream localFile, String remotePath) throws IOException {
        return mCard.uploadFile(remotePath, localFile);
    }

    public boolean deleteFile(VaultFile file) throws IOException {
        if (file.getType() == VaultFile.Type.FILE) {
            return mCard.deleteFile(file.getRemotePath());
        } else {
            return mCard.removeDirectory(file.getRemotePath());
        }
    }

    public boolean makeDirectory(String fullPath) throws IOException {
        return mCard.makeDirectory(fullPath);
    }

    public String getRootPath() {
        return mCard.getRootPath();
    }
}
