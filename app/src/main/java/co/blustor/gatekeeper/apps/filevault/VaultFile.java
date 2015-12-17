package co.blustor.gatekeeper.apps.filevault;

import java.io.File;

import co.blustor.gatekeeper.data.GKFile;

public class VaultFile extends GKFile {
    public static final String TAG = VaultFile.class.getSimpleName();

    private File mLocalPath;

    public VaultFile(GKFile file) {
        super(file.getName(), file.getType());
        setCardPath(file.getCardPath());
    }

    public File getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(File file) {
        mLocalPath = file;
    }
}
