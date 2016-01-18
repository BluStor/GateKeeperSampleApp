package co.blustor.gatekeeper.biometrics;

import android.content.Context;
import android.os.AsyncTask;

import com.neurotec.lang.NCore;
import com.neurotec.plugins.NDataFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeperdemo.R;

public class GKEnvironment {
    public static final String TAG = GKEnvironment.class.getSimpleName();

    private static GKEnvironment mInstance;
    private final Context mContext;
    private final GKLicensing mLicensing;

    private GKEnvironment(Context context) {
        mContext = context;
        mLicensing = new GKLicensing("/local", 5000);
    }

    public static GKEnvironment getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GKEnvironment(context);
        }
        return mInstance;
    }

    public AsyncTask<Void, Void, Void> initialize(final InitializationListener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ensureDataFilesExist();
                NCore.setContext(mContext);
                mLicensing.obtainLicenses();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                listener.onLicensesObtained();
            }
        };
        asyncTask.execute();
        return asyncTask;
    }

    public interface InitializationListener {
        void onLicensesObtained();
    }

    private void ensureDataFilesExist() {
        File facesFile = new File(mContext.getFilesDir(), "Faces.ndf");
        if (!facesFile.exists()) {
            try {
                InputStream is = mContext.getResources().openRawResource(R.raw.faces);
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
