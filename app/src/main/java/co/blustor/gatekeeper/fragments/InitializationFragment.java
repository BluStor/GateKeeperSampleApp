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
import co.blustor.gatekeeper.activities.AppLauncherActivity;
import co.blustor.gatekeeper.devices.GKAndroidClient;

public class InitializationFragment extends Fragment {
    public static final String TAG = InitializationFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    private AsyncTask<Void, Void, Void> mStartAppLauncherTask = new LoadingTask();
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
        startAppLauncher();
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void startAppLauncher() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            startAppLauncherWithDelay(elapsed);
        } else {
            startAppLauncherWithoutDelay();
        }
    }

    private void startAppLauncherWithoutDelay() {
        startActivity(new Intent(getActivity(), AppLauncherActivity.class));
        getActivity().finish();
    }

    private void startAppLauncherWithDelay(final long elapsedTime) {
        mStartAppLauncherTask = new LoadingTask() {
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
                    finish();
                    startAppLauncherWithoutDelay();
                }
            }

            @Override
            protected void onCancelled(Void aVoid) {
                super.onCancelled(aVoid);
                finish();
            }

            private void finish() {
                mStartAppLauncherTask = new LoadingTask();
            }
        }.execute();
    }

    public void cancel() {
        mStartAppLauncherTask.cancel(true);
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    private DialogFragment mRequestPairDialog = new RequestPairDialogFragment();
}
