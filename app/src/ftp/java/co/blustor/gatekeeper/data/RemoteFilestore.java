package co.blustor.gatekeeper.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import co.blustor.gatekeeper.net.FTPFilestore;
import co.blustor.gatekeeper.util.StringUtils;

public class RemoteFilestore implements AsyncFilestore {
    public static String TAG = RemoteFilestore.class.getSimpleName();

    private final FTPFilestore mFTPFilestore;
    private Stack<String> mCurrentPath = new Stack<>();

    public RemoteFilestore() {
        mFTPFilestore = new FTPFilestore();
    }

    public void finish() {
        try {
            mFTPFilestore.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void listFiles(final Listener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mFTPFilestore.open();
                    listener.onListFiles(mFTPFilestore.listFiles(getCurrentPath()));
                } catch (IOException e) {
                    Log.e(TAG, "failed");
                    e.printStackTrace();
                    listener.onListFilesError();
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void getFile(final AbstractFile file, final File targetPath, final Listener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String fullPath = getCurrentPath() + "/" + file.getName();
                    File targetFile = new File(targetPath, file.getName());
                    File downloaded = mFTPFilestore.downloadFile(fullPath, targetFile);
                    listener.onGetFile(new DroidFilestore.CachedFile(downloaded));
                } catch (IOException e) {
                    Log.e(TAG, "failed to get file", e);
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

    private String getCurrentPath() {
        return "/" + StringUtils.join(mCurrentPath.toArray(), "/");
    }
}
