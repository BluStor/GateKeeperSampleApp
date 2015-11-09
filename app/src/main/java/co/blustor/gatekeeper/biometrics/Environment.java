package co.blustor.gatekeeper.biometrics;

import android.content.Context;
import android.os.AsyncTask;

import com.neurotec.lang.NCore;
import com.neurotec.plugins.NDataFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Environment {
    public static String TAG = Environment.class.getSimpleName();

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private static Environment mInstance;
    private final Context mContext;
    private final Licensing mLicensing;

    private Environment(Context context) {
        mContext = context;
        mLicensing = new Licensing("/local", 5000);
    }

    public static Environment getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Environment(context);
        }
        return mInstance;
    }

    public AsyncTask<Void, Void, Void> initialize(final InitializationListener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                long startTime = System.nanoTime();
                ensureDataFilesExist();
                NCore.setContext(mContext);
                mLicensing.obtainLicenses();
                long elapsed = System.nanoTime() - startTime;
                if (elapsed < DELAY) {
                    try {
                        Thread.sleep((DELAY - elapsed) / NANOS_IN_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                listener.onComplete();
            }
        };
        asyncTask.execute();
        return asyncTask;
    }

    public interface InitializationListener {
        void onComplete();
    }

    private void ensureDataFilesExist() {
        File facesFile = new File(mContext.getFilesDir(), "Faces.ndf");
        if (!facesFile.exists()) {
            try {
                InputStream is = mContext.getAssets().open("Faces.ndf");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(facesFile);
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        NDataFileManager.getInstance().addFile(facesFile.getAbsolutePath());
    }
}
