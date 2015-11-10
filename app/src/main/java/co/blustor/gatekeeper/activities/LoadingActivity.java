package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class LoadingActivity extends Activity implements Environment.InitializationListener {
    public static final String TAG = LoadingActivity.class.getSimpleName();

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Environment env = Environment.getInstance(this);

        mLoadingStartTime = System.nanoTime();
        env.initialize(this);
    }

    @Override
    public void onLicensesObtained() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            artificialDelay(elapsed);
        } else {
            startFaceAuth();
        }
    }

    private void startFaceAuth() {
        Datastore datastore = DroidDatastore.getInstance(this);
        if (datastore.hasTemplate()) {
            startActivity(new Intent(this, AuthenticationActivity.class));
        } else {
            startActivity(new Intent(this, EnrollmentActivity.class));
        }
        finish();
    }

    private void artificialDelay(final long elapsedTime) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep((DELAY - elapsedTime) / NANOS_IN_MILLIS);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Artificial Delay Interrupted", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                startFaceAuth();
            }
        }.execute();
    }
}
