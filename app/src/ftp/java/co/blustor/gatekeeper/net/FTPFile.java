package co.blustor.gatekeeper.net;


public class FTPFile {
    public enum TYPE {
        FILE,
        DIRECTORY
    }

    String mName;
    TYPE mType;

    public FTPFile(String name, TYPE type) {
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
