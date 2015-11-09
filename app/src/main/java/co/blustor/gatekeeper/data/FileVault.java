package co.blustor.gatekeeper.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileVault {
    private static final String TAG = FileVault.class.getSimpleName();

    private LocalFilestore mLocalFilestore;
    private RemoteFilestore mRemoteFilestore;

    public FileVault(LocalFilestore localFilestore, RemoteFilestore remoteFilestore) {
        mLocalFilestore = localFilestore;
        mRemoteFilestore = remoteFilestore;
    }

    public void listFiles(final ListFilesListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    listener.onListFiles(mRemoteFilestore.listFiles());
                } catch (IOException e) {
                    Log.e(TAG, "Problem listing Files with FilestoreClient", e);
                    listener.onListFilesError(e);
                }
                return null;
            }
        }.execute();
    }

    public void listFiles(VaultFile file, ListFilesListener listener) {
        mRemoteFilestore.navigateTo(file.getName());
        listFiles(listener);
    }

    public void getFile(final VaultFile file, final GetFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File targetPath;
                try {
                    targetPath = mLocalFilestore.makeTempPath();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create local cache path", e);
                    listener.onGetFileError(e);
                    return null;
                }
                try {
                    file.setLocalPath(new File(targetPath, file.getName()));
                    mRemoteFilestore.getFile(file);
                    listener.onGetFile(file);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to get File", e);
                    listener.onGetFileError(e);
                }
                return null;
            }
        }.execute();
    }

    public void putFile(final InputStream localFile, final String filename, final PutFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mRemoteFilestore.putFile(localFile, filename);
                    listener.onPutFile();
                } catch(IOException e) {
                    Log.e(TAG, "Error uploading file.  Stack trace follows.", e);
                    listener.onPutFileError(e);
                }

                return null;
            }
        }.execute();
    }

    public void deleteFile(final VaultFile file, final DeleteFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if(mRemoteFilestore.deleteFile(file)) {
                        listener.onDeleteFile(file);
                    } else {
                        listener.onDeleteFileError(file, null);
                    }
                } catch(IOException e) {
                    Log.e(TAG, "Error deleting file.  Stack trace follows.", e);
                    listener.onDeleteFileError(file, e);
                }

                return null;
            }
        }.execute();
    }

    public void navigateUp() {
        mRemoteFilestore.navigateUp();
    }

    public void finish() {
        mRemoteFilestore.finish();
    }

    public void clearCache() {
        mLocalFilestore.clearCache();
    }

    public boolean isAtRoot() {
        return mRemoteFilestore.isAtRoot();
    }

    public interface ListFilesListener {
        void onListFiles(List<VaultFile> files);
        void onListFilesError(IOException e);
    }

    public interface GetFileListener {
        void onGetFile(VaultFile file);
        void onGetFileError(IOException e);
    }

    public interface PutFileListener {
        void onPutFile();
        void onPutFileError(IOException e);
    }

    public interface DeleteFileListener {
        void onDeleteFile(VaultFile file);
        void onDeleteFileError(VaultFile file, IOException e);
    }
}
