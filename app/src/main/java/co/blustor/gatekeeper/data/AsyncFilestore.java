package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AsyncFilestore {
    void listFiles(Listener listener);

    void getFile(AbstractFile file, File targetFile, Listener listener);

    void navigateTo(String path);

    void navigateUp();

    void finish();

    interface Listener {
        void onListFiles(List<AbstractFile> files);

        void onListFilesError();

        void onGetFile(AbstractFile file);

        void onGetFileError(IOException e);
    }
}
