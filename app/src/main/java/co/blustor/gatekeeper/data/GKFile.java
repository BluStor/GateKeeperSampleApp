package co.blustor.gatekeeper.data;

import co.blustor.gatekeeper.util.FileUtils;

public class GKFile {
    public final static String TAG = GKFile.class.getSimpleName();

    public enum Type {
        FILE,
        DIRECTORY
    }

    protected String mCardPath;
    protected String mName;
    protected Type mType;

    public GKFile(String name, Type type) {
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public boolean isDirectory() {
        return mType == Type.DIRECTORY;
    }

    public String getExtension() {
        if (mType == Type.DIRECTORY) { return null; }
        String[] parts = mName.split("\\.");
        String ext = (parts.length > 1) ? parts[parts.length - 1] : null;
        return ext;
    }

    public String getCardPath() {
        return mCardPath;
    }

    public void setCardPath(String fullPath) {
        mCardPath = fullPath;
    }

    public void setCardPath(String parentPath, String fileName) {
        setCardPath(FileUtils.joinPath(parentPath, fileName));
    }
}
