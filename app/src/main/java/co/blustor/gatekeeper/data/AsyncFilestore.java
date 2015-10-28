package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AsyncFilestore {
    void listFiles(Listener listener);

    void getFile(VaultFile file, File targetFile, Listener listener);

    void navigateTo(String path);

    void navigateUp();

    void finish();

    interface Listener {
        void onListFiles(List<VaultFile> files);

        void onListFilesError();

        void onGetFile(VaultFile file);

        void onGetFileError(IOException e);
    }
}
