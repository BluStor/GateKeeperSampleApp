package co.blustor.gatekeeper.data;

import java.util.Arrays;

import co.blustor.gatekeeper.util.StringUtils;

public class AbstractFile {
    private String mName;
    private Type mType;

    public enum Type {
        DIRECTORY,
        FILE
    }

    public AbstractFile(String name, Type type) {
        setName(name);
        setType(type);
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
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

    protected void setName(String name) {
        mName = name;
    }

    protected void setType(Type type) {
        mType = type;
    }
}
