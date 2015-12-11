package co.blustor.gatekeeper.apps.filevault;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.data.LocalFilestore;
import co.blustor.gatekeeper.data.GKFileBrowser;

public class FileVault {
    public static final String TAG = FileVault.class.getSimpleName();

    private LocalFilestore mLocalFilestore;
    private GKFileBrowser mFileBrowser;

    public FileVault(LocalFilestore localFilestore, GKFileBrowser fileBrowser) {
        mLocalFilestore = localFilestore;
        mFileBrowser = fileBrowser;
    }

    public void listFiles(final ListFilesListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;
            private List<VaultFile> mFiles;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mFiles = mFileBrowser.listFiles();
                } catch (IOException e) {
                    Log.e(TAG, "Problem listing Files with FilestoreClient", e);
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mException == null) {
                    listener.onListFiles(mFiles);
                } else {
                    listener.onListFilesError(mException);
                }
            }
        }.execute();
    }

    public void listFiles(VaultFile file, ListFilesListener listener) {
        mFileBrowser.navigateTo(file.getName());
        listFiles(listener);
    }

    public void getFile(final VaultFile file, final GetFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;

            @Override
            protected Void doInBackground(Void... params) {
                File targetPath;
                try {
                    targetPath = mLocalFilestore.makeTempPath();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create local cache path", e);
                    mException = e;
                    return null;
                }
                try {
                    file.setLocalPath(new File(targetPath, file.getName()));
                    mFileBrowser.getFile(file);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to Get File", e);
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mException == null) {
                    listener.onGetFile(file);
                } else {
                    listener.onGetFileError(mException);
                }
            }
        }.execute();
    }

    public void putFile(final InputStream localFile, final String filename, final PutFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mFileBrowser.putFile(localFile, filename);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to Upload File", e);
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mException == null) {
                    listener.onPutFile();
                } else {
                    listener.onPutFileError(mException);
                }
            }
        }.execute();
    }

    public void deleteFile(final VaultFile file, final DeleteFileListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    boolean deleted = mFileBrowser.deleteFile(file);
                    if (!deleted) {
                        mException = new IOException("File Not Deleted");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to Delete File", e);
                    mException = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mException == null) {
                    listener.onDeleteFile(file);
                } else {
                    listener.onDeleteFileError(file, mException);
                }
            }
        }.execute();
    }

    public void makeDirectory(final String directoryName, final MakeDirectoryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    boolean created = mFileBrowser.makeDirectory(directoryName);
                    if (!created) {
                        mException = new IOException("Directory Not Created");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to Create Directory", e);
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mException == null) {
                    listener.onMakeDirectory();
                } else {
                    listener.onMakeDirectoryError(mException);
                }
            }
        }.execute();
    }

    public void navigateUp() {
        mFileBrowser.navigateUp();
    }

    public void clearCache() {
        mLocalFilestore.clearCache();
    }

    public boolean isAtRoot() {
        return remoteAvailable() && mFileBrowser.isAtRoot();
    }

    public boolean remoteAvailable() {
        return mFileBrowser != null;
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

    public interface MakeDirectoryListener {
        void onMakeDirectory();
        void onMakeDirectoryError(IOException e);
    }
}
