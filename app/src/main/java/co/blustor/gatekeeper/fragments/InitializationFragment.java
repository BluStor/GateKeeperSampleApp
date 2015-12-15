package co.blustor.gatekeeper.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.activities.ActionBarActivity;
import co.blustor.gatekeeper.activities.AuthenticationActivity;
import co.blustor.gatekeeper.activities.EnrollmentActivity;
import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.biometrics.FaceCapture;
import co.blustor.gatekeeper.demo.Application;
import co.blustor.gatekeeper.devices.GKAndroidClient;

public class InitializationFragment extends Fragment implements Environment.InitializationListener {
    public static final String TAG = InitializationFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    private AsyncTask<Void, Void, Void> mStartFaceInitTask = new LoadingTask();
    private AsyncTask<Void, Void, Void> mStartFaceAuthTask = new LoadingTask();
    private GKAndroidClient mGKClient;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mLoadingStartTime = System.nanoTime();
        mGKClient = new GKAndroidClient();
        initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_initialization, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
            case REQUEST_CARD_PAIR:
                if (resultCode == Activity.RESULT_OK) {
                    initialize();
                } else {
                    getActivity().finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLicensesObtained() {
        startFaceCapture();
    }

    private void initialize() {
        mGKClient.initialize();
        if (!mGKClient.isBluetoothEnabled()) {
            requestBluetooth();
            return;
        }
        if (!mGKClient.isPairedWithCard()) {
            mRequestPairDialog.show(getFragmentManager(), "requestPairWithCard");
            return;
        }
        initializeFaceCapture();
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void initializeFaceCapture() {
        Environment.getInstance(getActivity()).initialize(this);
    }

    private void startFaceCapture() {
        final FaceCapture faceCapture = FaceCapture.getInstance();

        mStartFaceInitTask = new LoadingTask() {
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
                    mStartFaceInitTask = new LoadingTask();
                    startFaceAuth();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                faceCapture.discard();
            }
        }.execute();
    }

    private void startFaceAuth() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            startFaceAuthWithDelay(elapsed);
        } else {
            startFaceAuthWithoutDelay();
        }
    }

    private void startFaceAuthWithoutDelay() {
        Authentication authentication = Application.getAuthentication();
        boolean templateExists = authentication.listTemplates().size() > 0;
        if (templateExists) {
            startActivity(new Intent(getActivity(), AuthenticationActivity.class));
        } else {
            startActivity(new Intent(getActivity(), EnrollmentActivity.class));
        }
        getActivity().finish();
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
                    mStartFaceInitTask = new LoadingTask();
                    startFaceAuthWithoutDelay();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                FaceCapture.getInstance().discard();
            }
        }.execute();
    }

    public void cancel() {
        mStartFaceInitTask.cancel(true);
        mStartFaceAuthTask.cancel(true);
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    private DialogFragment mRequestPairDialog = new RequestPairDialogFragment();
}
