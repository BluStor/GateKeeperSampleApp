package co.blustor.gatekeeperdemo.filevault;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import co.blustor.gatekeeper.data.GKFile;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKFileActions;
import co.blustor.gatekeeper.utils.GKFileUtils;

public class FileVault {
    public static final String TAG = FileVault.class.getSimpleName();

    private Stack<String> mCurrentPath = new Stack<>();

    private final LocalFilestore mLocalFilestore;
    private final GKCard mGKCard;
    private final GKFileActions mFileActions;

    public FileVault(LocalFilestore localFilestore, GKCard gkCard) {
        mLocalFilestore = localFilestore;
        mGKCard = gkCard;
        mFileActions = new GKFileActions(gkCard);
    }

    public void setPath(String path) {
        mCurrentPath.clear();
        ArrayList<String> subPaths = GKFileUtils.parsePath(path);
        for (String subPath : subPaths) {
            mCurrentPath.push(subPath);
        }
    }

    public void listFiles(final ListFilesListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private IOException mException;
            private List<VaultFile> mFiles;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mFiles = new ArrayList<>();
                    GKFileActions.ListFilesResult result = mFileActions.listFiles(getCurrentPath());
                    List<GKFile> gkFiles = result.getFiles();
                    for (GKFile gkFile : gkFiles) {
                        mFiles.add(new VaultFile(gkFile));
                    }
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
        mCurrentPath.push(file.getName());
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
                    File localFile = new File(targetPath, file.getName());
                    file.setLocalPath(localFile);
                    mFileActions.getFile(file, localFile);
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
                    String fullPath = GKFileUtils.joinPath(getCurrentPath(), filename);
                    mFileActions.putFile(localFile, fullPath);
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
                    String fullPath = GKFileUtils.joinPath(getCurrentPath(), file.getName());
                    file.setCardPath(fullPath);
                    GKFileActions.FileResult result = mFileActions.deleteFile(file);
                    if (result.getStatus() != GKFileActions.Status.SUCCESS) {
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
                    String fullPath = GKFileUtils.joinPath(getCurrentPath(), directoryName);
                    GKFileActions.FileResult result = mFileActions.makeDirectory(fullPath);
                    if (result.getStatus() != GKFileActions.Status.SUCCESS) {
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
        if (!mCurrentPath.empty()) {
            mCurrentPath.pop();
        }
    }

    public void clearCache() {
        mLocalFilestore.clearCache();
    }

    public boolean isAtRoot() {
        return cardAvailable() && mCurrentPath.empty();
    }

    public boolean cardAvailable() {
        if (mGKCard == null) {
            return false;
        }
        try {
            mGKCard.connect();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String getCurrentPath() {
        String subPath = GKFileUtils.joinPath(mCurrentPath.toArray());
        return GKFileUtils.joinPath(GKFileUtils.ROOT, subPath);
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
