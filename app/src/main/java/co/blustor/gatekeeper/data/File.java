package co.blustor.gatekeeper.data;

public interface File {
    enum Type {
        DIRECTORY,
        FILE
    }

    String getName();
    Type getType();
}
