package co.blustor.gatekeeper.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Stack;

import co.blustor.gatekeeper.net.FTPFilestore;
import co.blustor.gatekeeper.util.StringUtils;

public class FTPAsyncFilestore implements AsyncFilestore {
    public static String TAG = FTPAsyncFilestore.class.getSimpleName();

    private final FTPFilestore mFTPFilestore;
    private Stack<String> mCurrentPath = new Stack<>();

    public FTPAsyncFilestore() {
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
