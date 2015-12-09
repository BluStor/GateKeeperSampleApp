package co.blustor.gatekeeper.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.activities.AuthenticationActivity;
import co.blustor.gatekeeper.activities.EnrollmentActivity;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.biometrics.FaceCapture;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;
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
                initialize();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
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

    private void requestPairWithCard() {
        Intent enableBtIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivityForResult(enableBtIntent, REQUEST_CARD_PAIR);
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
        Datastore datastore = DroidDatastore.getInstance(getActivity());
        if (datastore.hasTemplate()) {
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

    private DialogFragment mRequestPairDialog = new DialogFragment() {
        @Override
        public void onDestroyView() {
            if (getDialog() != null && getRetainInstance()) {
                getDialog().setDismissMessage(null);
            }
            super.onDestroyView();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);
            setCancelable(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.gk_pair_requested_title))
                   .setMessage(getString(R.string.gk_pair_requested_message))
                   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           requestPairWithCard();
                       }
                   })
                   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           getActivity().finish();
                       }
                   });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getActivity().finish();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            return builder.create();
        }
    };
}
