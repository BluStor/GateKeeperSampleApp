package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.dialogs.OkCancelDialogFragment;
import co.blustor.gatekeeperdemo.dialogs.RequestBluetoothDialogFragment;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.CardTaskFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;
import co.blustor.gatekeeperdemo.fragments.TestsFragment;

public abstract class CardActivity extends BaseActivity implements CardTaskFragment.Callbacks {
    private static final String TAG_PAIR_WITH_CARD = "PairWithCard";
    private static final String TAG_RETRY_CONNECT_CARD = "RetryConnectCard";
    private static final String TAG_SIGN_OUT = "SignOut";
    private static final String CONNECT_AUTOMATICALLY = "ConnectAutomatically";

    private static final int REQUEST_CARD_PAIR = 1;
    private static final int REQUEST_CAMERA_FOR_ENROLLMENT = 2;
    private static final int REQUEST_CAMERA_FOR_AUTHENTICATION = 3;

    protected GKCard mCard;
    protected GKFaces mFaces;

    private CardTaskFragment mTaskFragment;

    private boolean mConnectAutomatically = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCard = Application.getGKCard();

        if (savedInstanceState != null) {
            mConnectAutomatically = savedInstanceState.getBoolean(CONNECT_AUTOMATICALLY);
        }

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (CardTaskFragment) fm.findFragmentByTag(CardTaskFragment.TAG);

        if (mTaskFragment == null) {
            mTaskFragment = new CardTaskFragment();
            fm.beginTransaction().add(mTaskFragment, CardTaskFragment.TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionStateUI(mCard.getConnectionState());
        mCard.addMonitor(mCardMonitor);
        mTaskFragment.initialize();
    }

    @Override
    protected void onPause() {
        mCard.removeMonitor(mCardMonitor);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CONNECT_AUTOMATICALLY, mConnectAutomatically);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        CardFragment currentFragment = getCurrentFragment();
        if (canHandleBackPressed(currentFragment)) {
            currentFragment.navigateBack();
        } else if (isAtRootFragment() && isTaskRoot()) {
            promptSignOut();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CARD_PAIR:
                connectToCard();
                return;
            case REQUEST_CAMERA_FOR_ENROLLMENT:
            case REQUEST_CAMERA_FOR_AUTHENTICATION:
                if (resultCode == Activity.RESULT_OK) {
                    showPendingUI();
                    extractFaceData(requestCode, data);
                } else {
                    CardFragment fragment = getCurrentFragment();
                    if (fragment != null) {
                        fragment.onCardAccessUpdated();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onFacesReady(GKFaces faces) {
        mFaces = faces;
        CardFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.setFaces(faces);
        }
    }

    public void startEnrollment() {
        requestFacePhoto(REQUEST_CAMERA_FOR_ENROLLMENT);
    }

    public void startAuthentication() {
        requestFacePhoto(REQUEST_CAMERA_FOR_AUTHENTICATION);
    }

    public void startAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    protected void openSettings() {
        pushFragment(new SettingsFragment(), SettingsFragment.TAG);
    }

    protected void openTests() {
        pushFragment(new TestsFragment(), TestsFragment.TAG);
    }

    protected void promptEnableBluetooth() {
        String tag = RequestBluetoothDialogFragment.TAG;
        RequestBluetoothDialogFragment fragment = (RequestBluetoothDialogFragment) findDialog(tag);
        if (fragment == null) {
            fragment = new RequestBluetoothDialogFragment() {
                @Override
                protected void onBluetoothEnabled() {
                    connectToCard();
                }

                @Override
                protected void onCancel() {
                    finishAffinity();
                }
            };
            fragment.show(getSupportFragmentManager(), tag);
        }
    }

    protected void promptPairWithCard() {
        OkCancelDialogFragment fragment = (OkCancelDialogFragment) findDialog(TAG_PAIR_WITH_CARD);
        if (fragment == null) {
            fragment = new OkCancelDialogFragment() {
                @Override
                protected void onBuildDialog(AlertDialog.Builder builder) {
                    setTitle(R.string.gkcard_pair_requested_title);
                    setMessage(R.string.gkcard_pair_requested_message);
                    super.onBuildDialog(builder);
                }

                @Override
                protected void onOkay() {
                    Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    getActivity().startActivityForResult(intent, REQUEST_CARD_PAIR);
                    dismiss();
                }

                @Override
                protected void onCancel() {
                    finishAffinity();
                }
            };
            fragment.show(getSupportFragmentManager(), TAG_PAIR_WITH_CARD);
        }
    }

    protected void promptCardReconnect() {
        OkCancelDialogFragment fragment = (OkCancelDialogFragment) findDialog(TAG_RETRY_CONNECT_CARD);
        if (fragment == null) {
            fragment = new OkCancelDialogFragment() {
                @Override
                protected void onBuildDialog(android.support.v7.app.AlertDialog.Builder builder) {
                    setTitle(R.string.gkcard_reconnect_prompt_title);
                    setMessage(R.string.gkcard_reconnect_prompt_message);
                    setPositiveLabel(R.string.retry);
                    super.onBuildDialog(builder);
                }

                @Override
                protected void onOkay() {
                    connectToCard();
                    dismiss();
                }

                @Override
                protected void onCancel() {
                    finishAffinity();
                }
            };
            fragment.show(getSupportFragmentManager(), TAG_RETRY_CONNECT_CARD);
        }
    }

    protected void promptSignOut() {
        OkCancelDialogFragment fragment = (OkCancelDialogFragment) findDialog(TAG_SIGN_OUT);
        if (fragment == null) {
            fragment = new OkCancelDialogFragment() {
                @Override
                protected void onBuildDialog(android.support.v7.app.AlertDialog.Builder builder) {
                    setMessage(R.string.sign_out_confirm);
                    setPositiveLabel(R.string.sign_out_yes);
                    setNegativeLabel(R.string.sign_out_no);
                    super.onBuildDialog(builder);
                }

                @Override
                protected void onOkay() {
                    onSignOut();
                    dismiss();
                }
            };
            fragment.show(getSupportFragmentManager(), TAG_SIGN_OUT);
        }
    }

    protected void onSignOut() {
        final CardActivity activity = this;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    GKAuthentication auth = new GKAuthentication(mCard);
                    auth.signOut();
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.finishAffinity();
            }
        }.execute();
    }

    protected void showPendingUI() {
        CardFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.showPendingUI();
        }
    }

    protected void showMessage(int messageResource) {
        CardFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.showMessage(messageResource);
        }
    }

    @Override
    protected void pushFragment(CardFragment fragment, String tag) {
        fragment.setCard(mCard);
        super.pushFragment(fragment, tag);
    }

    private DialogFragment findDialog(String tag) {
        return (DialogFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    private void connectToCard() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
                } catch (IOException e) {
                }
                return null;
            }
        }.execute();
    }

    private void requestFacePhoto(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    protected void updateConnectionStateUI(GKCard.ConnectionState state) {
        switch (state) {
            case BLUETOOTH_DISABLED:
                promptEnableBluetooth();
                return;
            case CARD_NOT_PAIRED:
                promptPairWithCard();
                return;
            case DISCONNECTED:
                if (mConnectAutomatically) {
                    mConnectAutomatically = false;
                    connectToCard();
                } else {
                    promptCardReconnect();
                }
        }
    }

    private void extractFaceData(final int requestCode, final Intent data) {
        final Bundle extras = data.getExtras();
        final boolean isAuthenticating = requestCode == REQUEST_CAMERA_FOR_AUTHENTICATION;

        new AsyncTask<Void, Void, GKAuthentication.Status>() {
            private IOException ioException;
            private final Bitmap bitmap = (Bitmap) extras.get("data");
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    GKFaces.Template template = mFaces.createTemplateFromBitmap(bitmap);
                    if (isAuthenticating) {
                        return auth.signInWithFace(template).getStatus();
                    } else {
                        return auth.enrollWithFace(template).getStatus();
                    }
                } catch (IOException e) {
                    ioException = e;
                    return null;
                } finally {
                    bitmap.recycle();
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                if (ioException == null) {
                    if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                        showMessage(R.string.authentication_success_message);
                        startMainActivity();
                    } else if (status.equals(GKAuthentication.Status.TEMPLATE_ADDED)) {
                        showMessage(R.string.enrollment_success_prompt_message);
                    } else if (isAuthenticating) {
                        showMessage(R.string.authentication_failure_message);
                    } else {
                        showMessage(R.string.enrollment_failure_prompt_message);
                    }
                }
            }
        }.execute();
    }

    protected GKCard.Monitor mCardMonitor = new UICardMonitor() {
    };

    private abstract class UICardMonitor implements GKCard.Monitor {
        public final String TAG = UICardMonitor.class.getSimpleName();

        @Override
        public void onStateChanged(final GKCard.ConnectionState state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Card State Changed: " + state.toString());
                    updateConnectionStateUI(state);
                }
            });
        }
    }
}
