package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.neurotec.biometrics.NSubject;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKEnvironment;
import co.blustor.gatekeeper.biometrics.GKFaceExtractor;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public class AuthFragment extends CardFragment implements GKEnvironment.InitializationListener {
    public static final String TAG = AuthFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CARD_PAIR = 2;
    private static final int REQUEST_CAMERA_FOR_ENROLLMENT = 3;
    private static final int REQUEST_CAMERA_FOR_AUTHENTICATION = 4;

    private final Object mSyncObject = new Object();

    private boolean mInitializing;
    private boolean mLicensesReady;
    private boolean mEnrollmentChecked;
    private boolean mIsEnrolled;

    private GKFaceExtractor mFaceExtractor;

    private AsyncTask<Void, Void, Boolean> mInitGKCardTask = new LoadingTask();
    private AsyncTask<Void, Void, Boolean> mCheckEnrollmentTask = new LoadingTask();
    private AsyncTask<Void, Void, Boolean> mExtractFaceTask = new LoadingTask();
    private AsyncTask<Void, Void, GKAuthentication.Status> mBypassTask;

    private ProgressBar mProgressBar;
    private Button mEnroll;
    private Button mAuthenticate;
    private Button mDemoSetup;
    private Button mBypassAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mEnroll = (Button) view.findViewById(R.id.enroll);
        mEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFacePhoto(REQUEST_CAMERA_FOR_ENROLLMENT);
                mEnroll.setEnabled(false);
                mDemoSetup.setEnabled(false);
                mBypassAuth.setEnabled(false);
            }
        });
        mAuthenticate = (Button) view.findViewById(R.id.authenticate);
        mAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFacePhoto(REQUEST_CAMERA_FOR_AUTHENTICATION);
                mAuthenticate.setEnabled(false);
                mDemoSetup.setEnabled(false);
                mBypassAuth.setEnabled(false);
            }
        });
        mDemoSetup = (Button) view.findViewById(R.id.demo_setup);
        mBypassAuth = (Button) view.findViewById(R.id.bypass);
        mBypassAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bypassAuth();
                mEnroll.setEnabled(false);
                mAuthenticate.setEnabled(false);
                mDemoSetup.setEnabled(false);
                mBypassAuth.setEnabled(false);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
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
            case REQUEST_CAMERA_FOR_ENROLLMENT:
            case REQUEST_CAMERA_FOR_AUTHENTICATION:
                if (resultCode == Activity.RESULT_OK) {
                    extractFaceData(data);
                } else {
                    prepareUI();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void extractFaceData(final Intent data) {
        final Bundle extras = data.getExtras();

        new AsyncTask<Void, Void, GKAuthentication.Status>() {
            private IOException ioException;
            private final Bitmap bitmap = (Bitmap) extras.get("data");
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    NSubject subject = mFaceExtractor.getSubjectFromBitmap(bitmap);
                    if (subject != null) {
                        return auth.enrollWithFace(subject);
                    } else {
                        return GKAuthentication.Status.BAD_TEMPLATE;
                    }
                } catch (IOException e) {
                    ioException = e;
                    return null;
                } finally {
                    bitmap.recycle();
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status aVoid) {
                if (ioException != null) {
                    showRetryConnectDialog();
                } else if (aVoid.equals(GKAuthentication.Status.SUCCESS)) {
                    if (mIsEnrolled) {
                        showMessage(R.string.authentication_result_success);
                        startActivity(new Intent(getActivity(), CardActivity.class));
                        getActivity().finish();
                    } else {
                        showMessage(R.string.enrollment_result_success);
                        mEnroll.setVisibility(View.GONE);
                        mEnroll.setEnabled(false);
                        mAuthenticate.setVisibility(View.VISIBLE);
                        mAuthenticate.setEnabled(true);
                    }
                } else if (mIsEnrolled) {
                    showMessage(R.string.authentication_result_failure);
                    mEnroll.setVisibility(View.GONE);
                    mEnroll.setEnabled(false);
                    mAuthenticate.setVisibility(View.VISIBLE);
                    mAuthenticate.setEnabled(true);
                } else {
                    showMessage(R.string.enroll_failure_prompt_message);
                    mEnroll.setVisibility(View.VISIBLE);
                    mEnroll.setEnabled(true);
                    mAuthenticate.setVisibility(View.GONE);
                    mAuthenticate.setEnabled(false);
                }
            }
        }.execute();
    }

    private void bypassAuth() {
        mBypassTask = new AsyncTask<Void, Void, GKAuthentication.Status>() {
            private IOException ioException;
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    String templatePath = getAbsolutePath("GoodTemplate.dat");
                    NSubject subject = NSubject.fromFile(templatePath);
                    return auth.signInWithFace(subject);
                } catch (IOException e) {
                    ioException = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status aVoid) {
                mBypassTask = null;
                if (isCancelled()) {
                    return;
                }
                if (ioException != null) {
                    showRetryConnectDialog();
                } else {
                    startActivity(new Intent(getActivity(), CardActivity.class));
                    getActivity().finish();
                }

            }
        }.execute();
    }

    private void initialize() {
        synchronized (mSyncObject) {
            if (!mInitializing) {
                mInitializing = true;
                GKEnvironment.getInstance(getActivity()).initialize(this);
            }
            if (!mEnrollmentChecked) {
                checkForEnrollment();
            }
        }
    }

    private void checkForEnrollment() {
        mCheckEnrollmentTask = new LoadingTask() {
            @Override
            protected Boolean doInBackground(Void... params) {
                GKAuthentication authentication = new GKAuthentication(mCard);
                try {
                    return authentication.listTemplates().size() > 0;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean templateExists) {
                synchronized (mSyncObject) {
                    mEnrollmentChecked = true;
                    mIsEnrolled = templateExists;
                }
                checkInitialization();
            }
        }.execute();
    }

    private void preloadFaceExtractor() {
        new AsyncTask<Void, Void, GKFaceExtractor>() {
            @Override
            protected GKFaceExtractor doInBackground(Void... params) {
                return new GKFaceExtractor();
            }

            @Override
            protected void onPostExecute(GKFaceExtractor faceExtractor) {
                synchronized (mSyncObject) {
                    mFaceExtractor = faceExtractor;
                }
                checkInitialization();
            }
        }.execute();
    }

    private void checkInitialization() {
        synchronized (mSyncObject) {
            if (mLicensesReady && mFaceExtractor != null && mEnrollmentChecked) {
                prepareUI();
            }
        }
    }

    private void prepareUI() {
        if (mInitializing) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuthenticate.setVisibility(View.GONE);
            mEnroll.setVisibility(View.GONE);
            mDemoSetup.setEnabled(false);
            mBypassAuth.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mDemoSetup.setEnabled(true);
            mBypassAuth.setEnabled(mIsEnrolled);
            if (mIsEnrolled) {
                mAuthenticate.setVisibility(View.VISIBLE);
                mAuthenticate.setEnabled(true);
            } else {
                mEnroll.setVisibility(View.VISIBLE);
                mEnroll.setEnabled(true);
            }
        }
    }

    private void requestFacePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    private void showRetryConnectDialog() {
        mRetryConnectDialog.show(getFragmentManager(), "retryConnectToCard");
    }

    protected void showMessage(int messageResource) {
        Log.i(TAG, getString(messageResource));
        Toast toast = Toast.makeText(getContext(), messageResource, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @NonNull
    private String getAbsolutePath(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, filename).getAbsolutePath();
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

    @Override
    public void onLicensesObtained() {
        synchronized (mSyncObject) {
            mLicensesReady = true;
            mInitializing = false;
        }
        preloadFaceExtractor();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }
    }
}
