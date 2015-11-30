package co.blustor.gatekeeper.data;

import java.io.File;
import java.util.Arrays;

import co.blustor.gatekeeper.util.FileUtils;
import co.blustor.gatekeeper.util.StringUtils;

public class VaultFile {
    public static final String TAG = VaultFile.class.getSimpleName();

    private String mName;
    private Type mType;
    private String mRemotePath;
    private File mLocalPath;

    public enum Type {
        DIRECTORY,
        FILE
    }

    public VaultFile(String name, Type type) {
        setName(name);
        setType(type);
    }

    public static VaultFile fromRemotePath(String path, Type type) {
        String pathName = FileUtils.getPathName(path);
        VaultFile vaultFile = new VaultFile(pathName, type);
        vaultFile.setRemotePath(path);
        return vaultFile;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public String getRemotePath() {
        return mRemotePath;
    }

    public File getLocalPath() {
        return mLocalPath;
    }

    public String getBasename() {
        String[] parts = getName().split("\\.");
        if (parts.length > 1) {
            parts = Arrays.copyOfRange(parts, 0, parts.length - 1);
        }
        return StringUtils.join(parts, ".");
    }

    public String getExtension() {
        if (mType == Type.DIRECTORY) return null;
        String[] parts = mName.split("\\.");
        String ext = (parts.length > 1) ? parts[parts.length - 1] : null;
        return ext;
    }

    public void setLocalPath(File file) {
        mLocalPath = file;
    }

    protected void setName(String name) {
        mName = name;
    }

    protected void setType(Type type) {
        mType = type;
    }

    protected void setRemotePath(String fullPath) {
        mRemotePath = fullPath;
    }

    protected void setRemotePath(String parentPath, String fileName) {
        setRemotePath(FileUtils.joinPath(parentPath, fileName));
    }
}
