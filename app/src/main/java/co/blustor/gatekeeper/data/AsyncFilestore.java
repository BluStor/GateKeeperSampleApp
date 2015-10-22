package co.blustor.gatekeeper.data;

import java.util.List;

public interface AsyncFilestore {
    void listFiles(Listener listener);

    void navigateTo(String path);

    void navigateUp();

    interface Listener {
        void onListFiles(List<File> files);
    }
}
