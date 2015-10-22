package co.blustor.gatekeeper.data;

import java.util.List;

public interface AsyncFilestore {
    void listFiles(Listener listener);

    interface Listener {
        void onListFiles(List<File> files);
    }
}
