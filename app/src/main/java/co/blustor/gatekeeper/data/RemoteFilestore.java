package co.blustor.gatekeeper.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import co.blustor.gatekeeper.util.FileUtils;

public class RemoteFilestore implements AsyncFilestore {
    public static String TAG = RemoteFilestore.class.getSimpleName();

    private final RemoteFilestoreClient mFilestoreClient;
    private Stack<String> mCurrentPath = new Stack<>();

    public RemoteFilestore(RemoteFilestoreClient client) {
        mFilestoreClient = client;
    }

    @Override
    public void listFiles(final Listener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mFilestoreClient.open();
                    listener.onListFiles(mFilestoreClient.listFiles(getCurrentPath()));
                } catch (IOException e) {
                    Log.e(TAG, "Problem listing Files with FilestoreClient", e);
                    listener.onListFilesError();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void getFile(final AbstractFile file, final File targetPath, final Listener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String fullPath = getCurrentPath() + "/" + file.getName();
                    File targetFile = new File(targetPath, file.getName());
                    File downloaded = mFilestoreClient.downloadFile(fullPath, targetFile);
                    listener.onGetFile(new LocalFilestore.CachedFile(downloaded));
                } catch (IOException e) {
                    Log.e(TAG, "Unable to get File", e);
                    listener.onGetFileError(e);
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void navigateTo(String path) {
        mCurrentPath.push(path);
    }

    @Override
    public void navigateUp() {
        if (!mCurrentPath.empty()) {
            mCurrentPath.pop();
        }
    }

    @Override
    public void finish() {
        try {
            mFilestoreClient.close();
        } catch (IOException e) {
        }
    }

    private String getCurrentPath() {
        String rootPath = mFilestoreClient.getRootPath();
        String subPath = FileUtils.joinPath(mCurrentPath.toArray());
        return FileUtils.joinPath(rootPath, subPath);
    }
}
