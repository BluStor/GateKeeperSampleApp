package co.blustor.gatekeeper.data;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileVault {
    private static final String TAG = FileVault.class.getSimpleName();

    private LocalFilestore mLocalFilestore;
    private RemoteFilestore mRemoteFilestore;

    public FileVault(LocalFilestore localFilestore, RemoteFilestore remoteFilestore) {
        mLocalFilestore = localFilestore;
        mRemoteFilestore = remoteFilestore;
    }

    public void listFiles(ListFilesListener listener) {
        mRemoteFilestore.listFiles(wrapListFilesListener(listener));
    }

    public void listFiles(VaultFile file, ListFilesListener listener) {
        mRemoteFilestore.navigateTo(file.getName());
        mRemoteFilestore.listFiles(wrapListFilesListener(listener));
    }

    public void getFile(VaultFile file, GetFileListener listener) {
        try {
            File targetPath = mLocalFilestore.makeTempPath();
            mRemoteFilestore.getFile(file, targetPath, wrapGetFileListener(listener));
        } catch (IOException e) {
            Log.e(TAG, "Unable to create local cache path", e);
            listener.onGetFileError(e);
        }
    }

    public void navigateUp() {
        mRemoteFilestore.navigateUp();
    }

    public void finish() {
        mRemoteFilestore.finish();
    }

    public interface ListFilesListener {
        void onListFiles(List<VaultFile> files);
        void onListFilesError(IOException e);
    }

    public interface GetFileListener {
        void onGetFile(VaultFile file);
        void onGetFileError(IOException e);
    }

    private AsyncFilestore.Listener wrapListFilesListener(final ListFilesListener listener) {
        return new AsyncFilestore.Listener() {
            @Override
            public void onListFiles(List<VaultFile> files) {
                listener.onListFiles(files);
            }

            @Override
            public void onListFilesError() {
                listener.onListFilesError(new IOException());
            }

            @Override
            public void onGetFile(VaultFile file) {
            }

            @Override
            public void onGetFileError(IOException e) {
            }
        };
    }

    private AsyncFilestore.Listener wrapGetFileListener(final GetFileListener listener) {
        return new AsyncFilestore.Listener() {
            @Override
            public void onListFiles(List<VaultFile> files) {
            }

            @Override
            public void onListFilesError() {
            }

            @Override
            public void onGetFile(VaultFile file) {
                listener.onGetFile(file);
            }

            @Override
            public void onGetFileError(IOException e) {
                listener.onGetFileError(e);
            }
        };
    }
}
