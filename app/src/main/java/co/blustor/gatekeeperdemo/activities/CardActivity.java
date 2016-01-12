package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.CardTaskFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;
import co.blustor.gatekeeperdemo.fragments.TestsFragment;

public abstract class CardActivity extends BaseActivity implements CardTaskFragment.Callbacks {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;
    private static final int REQUEST_CAMERA_FOR_ENROLLMENT = 3;
    private static final int REQUEST_CAMERA_FOR_AUTHENTICATION = 4;

    protected GKCard mCard;
    protected GKFaces mFaces;

    private CardTaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCard = Application.getGKCard();

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
        connectWithCard();
        mTaskFragment.initialize();
    }

    private void connectWithCard() {
        try {
            mCard.connect();
            setCardAvailable(true);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
            setCardAvailable(false);
        }
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
            case REQUEST_ENABLE_BT:
            case REQUEST_CARD_PAIR:
                if (resultCode == Activity.RESULT_OK) {
                    connectWithCard();
                } else {
                    finishAffinity();
                }
                break;
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
                    updateUI();
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

    public void onCardReady() {
        setCardAvailable(true);
    }

    public void showRetryConnectDialog() {
        mRetryConnectDialog.show(getSupportFragmentManager(), "retryConnectToCard");
    }

    public void startEnrollment() {
        requestFacePhoto(REQUEST_CAMERA_FOR_ENROLLMENT);
    }

    public void startAuthentication() {
        requestFacePhoto(REQUEST_CAMERA_FOR_AUTHENTICATION);
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

    protected void promptSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sign_out_confirm);
        builder.setPositiveButton(R.string.sign_out_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSignOut();
            }
        });
        builder.setNegativeButton(R.string.sign_out_no, null);
        builder.create().show();
    }

    protected void onSignOut() {
        final Activity activity = this;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
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

    protected void updateUI() {
        CardFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.updateUI();
        }
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

    protected void connectToCard() {
        setCardAvailable(false);
        new AsyncTask<Void, Void, Void>() {
            private IOException ioException;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
                } catch (IOException e) {
                    ioException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (ioException != null) {
                    showRetryConnectDialog();
                } else {
                    onCardReady();
                }
            }
        }.execute();
    }

    private void setCardAvailable(boolean available) {
        CardFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.setCardAvailable(available);
        }
    }

    private void requestFacePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    private void extractFaceData(final int requestCode, final Intent data) {
        final Bundle extras = data.getExtras();
        final Activity activity = this;
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
                if (ioException != null) {
                    showRetryConnectDialog();
                } else if (status.equals(GKAuthentication.Status.TEMPLATE_ADDED)) {
                    showMessage(R.string.enrollment_success_prompt_message);
                } else if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                    showMessage(R.string.authentication_success_message);
                    startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                    return;
                } else if (isAuthenticating) {
                    showMessage(R.string.authentication_failure_message);
                } else {
                    showMessage(R.string.enrollment_failure_prompt_message);
                }
                setCardAvailable(true);
                updateUI();
            }
        }.execute();
    }

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
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.gkcard_reconnect_prompt_title))
                   .setMessage(getString(R.string.gkcard_reconnect_prompt_message))
                   .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           connectToCard();
                       }
                   })
                   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           getActivity().finishAffinity();
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
