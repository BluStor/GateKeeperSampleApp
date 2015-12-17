package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.devices.GKCard;

public class GKFileActions {
    public static final String TAG = GKFileActions.class.getSimpleName();

    private final GKCard mCard;

    public GKFileActions(GKCard card) {
        mCard = card;
    }

    public List<GKFile> listFiles(String remotePath) throws IOException {
        return mCard.listFiles(remotePath);
    }

    public File getFile(final GKFile gkFile, File localFile) throws IOException {
        return mCard.downloadFile(gkFile, localFile);
    }

    public boolean putFile(InputStream localFile, String remotePath) throws IOException {
        return mCard.uploadFile(remotePath, localFile);
    }

    public boolean deleteFile(GKFile file) throws IOException {
        if (file.getType() == GKFile.Type.FILE) {
            return mCard.deleteFile(file.getCardPath());
        } else {
            return mCard.removeDirectory(file.getCardPath());
        }
    }

    public boolean makeDirectory(String fullPath) throws IOException {
        return mCard.makeDirectory(fullPath);
    }

    public String getRootPath() {
        return mCard.getRootPath();
    }
}
