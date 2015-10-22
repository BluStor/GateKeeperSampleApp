package co.blustor.gatekeeper.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.net.FTPFilestore;

public class FTPAsyncFilestore implements AsyncFilestore {
    public static String TAG = FTPAsyncFilestore.class.getSimpleName();

    private final FTPFilestore mFTPFilestore;

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
                    listener.onListFiles(mFTPFilestore.listFiles("/"));
                } catch (IOException e) {
                    Log.e(TAG, "failed");
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute();
    }
}
