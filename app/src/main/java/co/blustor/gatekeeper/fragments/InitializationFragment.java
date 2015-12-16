package co.blustor.gatekeeper.fragments;

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

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.activities.AppLauncherActivity;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;

public class InitializationFragment extends Fragment {
    public static final String TAG = InitializationFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;

    public static final int NANOS_IN_MILLIS = 1000000;
    public static final long DELAY = NANOS_IN_MILLIS * 1000;

    private long mLoadingStartTime;

    private AsyncTask<Void, Void, Void> mStartAppLauncherTask = new LoadingTask();

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

    private void initialize() {
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
            mRequestPairDialog.show(getFragmentManager(), "requestPairWithCard");
        }
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void checkCardConnection(final GKCard card) {
        new AsyncTask<Void, Void, Boolean>() {
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
                    startAppLauncher();
                } else {
                    showRetryConnectDialog();
                }
            }
        }.execute();
    }

    private void startAppLauncher() {
        long elapsed = System.nanoTime() - mLoadingStartTime;
        if (elapsed < DELAY) {
            startAppLauncherWithDelay(elapsed);
        } else {
            startAppLauncherWithoutDelay();
        }
    }

    private void showRetryConnectDialog() {
        mRetryConnectDialog.show(getFragmentManager(), "retryConnectToCard");
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
