package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
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

import java.io.IOException;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.AuthenticationActivity;
import co.blustor.gatekeeperdemo.activities.EnrollmentActivity;
import co.blustor.gatekeeper.authentication.GKCardAuthentication;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.biometrics.GKFaceCapture;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;

public class InitializationFragment extends Fragment implements Environment.InitializationListener {
    public static final String TAG = InitializationFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    private final Object mSyncObject = new Object();

    private boolean mFaceCaptureReady;
    private boolean mGKCardReady;

    private AsyncTask<Void, Void, Boolean> mInitFaceCaptureTask = new LoadingTask();
    private AsyncTask<Void, Void, Boolean> mInitGKCardTask = new LoadingTask();
    private AsyncTask<Void, Void, Boolean> mCheckAuthTask = new LoadingTask();
    private AsyncTask<Void, Void, Boolean> mCompleteInitTask = new LoadingTask();

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mLoadingStartTime = System.nanoTime();
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

    public void cancel() {
        mInitFaceCaptureTask.cancel(true);
        mInitGKCardTask.cancel(true);
        mCompleteInitTask.cancel(true);
    }

    private void checkInitialization() {
        synchronized (mSyncObject) {
            if (mFaceCaptureReady && mGKCardReady) {
                onInitializationComplete();
            }
        }
    }

    private void initialize() {
        initializeFaceCapture();
        try {
            GKCard card = GKCardConnector.find();
            checkCardConnection(card);
        } catch (GKCardConnector.BluetoothUnavailableException e) {
            Log.e(TAG, e.getMessage(), e);
            getActivity().finish();
        } catch (GKCardConnector.BluetoothDisabledException e) {
            Log.e(TAG, e.getMessage(), e);
            requestBluetooth();
        } catch (GKCardConnector.GKCardNotFound e) {
            Log.e(TAG, e.getMessage(), e);
            requestPairWithCard();
        }
    }

    private void checkCardConnection(final GKCard card) {
        mInitGKCardTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    card.connect();
                    card.disconnect();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean canConnect) {
                if (canConnect) {
                    synchronized (mSyncObject) {
                        mGKCardReady = true;
                        checkInitialization();
                    }
                } else {
                    showRetryConnectDialog();
                }
            }
        };
        mInitGKCardTask.execute();
    }

    private void initializeFaceCapture() {
        Environment.getInstance(getActivity()).initialize(this);
    }

    private void startFaceCapture() {
        final GKFaceCapture faceCapture = GKFaceCapture.getInstance();

        mInitFaceCaptureTask = new LoadingTask() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (!isCancelled()) {
                    faceCapture.start();
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean _) {
                super.onPostExecute(_);
                if (!isCancelled()) {
                    mInitFaceCaptureTask = new LoadingTask();
                    synchronized (mSyncObject) {
                        mFaceCaptureReady = true;
                        checkInitialization();
                    }
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                faceCapture.discard();
            }
        }.execute();
    }

    private void onInitializationComplete() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            completeWithDelay(elapsed);
        } else {
            completeWithoutDelay();
        }
    }

    private void completeWithoutDelay() {
        mCheckAuthTask = new LoadingTask() {
            @Override
            protected Boolean doInBackground(Void... params) {
                GKCardAuthentication authentication = Application.getAuthentication();
                try {
                    return authentication.listTemplates().size() > 0;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean templateExists) {
                if (templateExists) {
                    startActivity(new Intent(getActivity(), AuthenticationActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), EnrollmentActivity.class));
                }
                getActivity().finish();
            }
        }.execute();
    }

    private void completeWithDelay(final long elapsedTime) {
        mCompleteInitTask = new LoadingTask() {
            @Override
            protected Boolean doInBackground(Void... params) {
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
            protected void onPostExecute(Boolean _) {
                if (!isCancelled()) {
                    super.onPostExecute(_);
                    finish();
                    completeWithoutDelay();
                }
            }

            @Override
            protected void onCancelled(Boolean _) {
                super.onCancelled(_);
                finish();
            }

            private void finish() {
                mCompleteInitTask = new LoadingTask();
            }
        }.execute();
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void requestPairWithCard() {
        mRequestPairDialog.show(getFragmentManager(), "requestPairWithCard");
    }

    private void showRetryConnectDialog() {
        mRetryConnectDialog.show(getFragmentManager(), "retryConnectToCard");
    }

    private class LoadingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }
    }

    private DialogFragment mRequestPairDialog = new RequestPairDialogFragment();

    private DialogFragment mRetryConnectDialog = new DialogFragment() {
        public final String TAG = DialogFragment.class.getSimpleName();

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
            builder.setTitle(getString(R.string.gk_retry_connect_card_title))
                   .setMessage(getString(R.string.gk_retry_connect_card_message))
                   .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           initialize();
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
