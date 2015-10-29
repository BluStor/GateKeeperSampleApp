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

    public enum Status {
        OK,
        PREPARING,
        NO_LICENSE
    }

    private static Environment mInstance;
    private final Context mContext;
    private final Licensing mLicensing;

    private Environment(Context context) {
        mContext = context;
        mLicensing = new Licensing();
    }

    public static Environment getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Environment(context);
        }
        return mInstance;
    }

    public AsyncTask<Void, Void, Status> initialize(final InitializationListener listener) {
        AsyncTask<Void, Void, Status> asyncTask = new AsyncTask<Void, Void, Status>() {
            @Override
            protected Environment.Status doInBackground(Void... params) {
                listener.onStatusChanged(Environment.Status.PREPARING);
                ensureDataFilesExist();
                NCore.setContext(mContext);
                mLicensing.obtainLicenses();
                return Environment.Status.OK;
            }

            @Override
            protected void onPostExecute(Environment.Status status) {
                super.onPostExecute(status);
                listener.onComplete(Environment.Status.OK);
            }
        };
        asyncTask.execute();
        return asyncTask;
    }

    public interface InitializationListener {
        void onStatusChanged(Status status);

        void onComplete(Status status);
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
