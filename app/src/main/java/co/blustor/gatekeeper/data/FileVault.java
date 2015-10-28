package co.blustor.gatekeeper.data;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FileVault {
    private static final String TAG = FileVault.class.getSimpleName();

    private LocalFilestore mLocalFilestore;
    private RemoteFilestore mRemoteFilestore;

    public FileVault(LocalFilestore localFilestore, RemoteFilestore remoteFilestore) {
        mLocalFilestore = localFilestore;
        mRemoteFilestore = remoteFilestore;
    }

    public void listFiles(AsyncFilestore.Listener listener) {
        mRemoteFilestore.listFiles(listener);
    }

    public void listFiles(VaultFile file, AsyncFilestore.Listener listener) {
        mRemoteFilestore.navigateTo(file.getName());
        mRemoteFilestore.listFiles(listener);
    }

    public void getFile(VaultFile file, AsyncFilestore.Listener listener) {
        try {
            File targetPath = mLocalFilestore.makeTempPath();
            mRemoteFilestore.getFile(file, targetPath, listener);
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
}
