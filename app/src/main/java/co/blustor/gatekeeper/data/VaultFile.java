package co.blustor.gatekeeper.data;

import java.io.File;
import java.util.Arrays;

import co.blustor.gatekeeper.util.FileUtils;
import co.blustor.gatekeeper.util.StringUtils;

public class VaultFile {
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

    protected void setRemotePath(String parentPath, String fileName) {
        mRemotePath = FileUtils.joinPath(parentPath, fileName);
    }
}
