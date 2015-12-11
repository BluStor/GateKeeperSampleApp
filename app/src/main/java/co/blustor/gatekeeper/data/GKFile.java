package co.blustor.gatekeeper.data;

public class GKFile {
    public final static String TAG = GKFile.class.getSimpleName();

    public enum TYPE {
        FILE,
        DIRECTORY
    }

    String mName;
    TYPE mType;

    public GKFile(String name, TYPE type) {
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public boolean isDirectory() {
        return mType == TYPE.DIRECTORY;
    }
}
