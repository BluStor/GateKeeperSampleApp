package co.blustor.gatekeeper.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.biometrics.FaceCapture;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class LoadingActivity extends BaseActivity implements Environment.InitializationListener {
    public static final String TAG = LoadingActivity.class.getSimpleName();

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    private AsyncTask<Void, Void, Void> mStartFaceTask = new LoadingTask();
    private AsyncTask<Void, Void, Void> mStartFaceAuthTask = new LoadingTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Environment env = Environment.getInstance(this);

        mLoadingStartTime = System.nanoTime();
        env.initialize(this);
    }

    @Override
    public void onBackPressed() {
        mStartFaceTask.cancel(true);
        mStartFaceAuthTask.cancel(true);
        super.onBackPressed();
    }

    @Override
    public void onLicensesObtained() {
        startFaceCapture();
    }

    private void startFaceCapture() {
        final FaceCapture faceCapture = FaceCapture.getInstance();

        mStartFaceTask = new LoadingTask() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!isCancelled()) {
                    faceCapture.start();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (!isCancelled()) {
                    mStartFaceTask = new LoadingTask();
                    onFaceCaptureStarted();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                faceCapture.discard();
            }
        }.execute();
    }

    private void onFaceCaptureStarted() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            startFaceAuthWithDelay(elapsed);
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

    private void startFaceAuthWithDelay(final long elapsedTime) {
        mStartFaceAuthTask = new LoadingTask() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!isCancelled()) {
                    try {
                        Thread.sleep((DELAY - elapsedTime) / NANOS_IN_MILLIS);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Artificial Delay Interrupted", e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!isCancelled()) {
                    super.onPostExecute(aVoid);
                    mStartFaceTask = new LoadingTask();
                    startFaceAuth();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                FaceCapture.getInstance().discard();
            }
        }.execute();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }
}
