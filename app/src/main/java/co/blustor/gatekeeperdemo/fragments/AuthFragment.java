package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKEnvironment;
import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.DemoSetupActivity;
import co.blustor.gatekeeperdemo.activities.MainActivity;
import co.blustor.gatekeeperdemo.utils.DemoHelper;

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

    private GKFaces mFaces;
    private DemoHelper mDemoHelper;

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
        mDemoSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DemoSetupActivity.class));
            }
        });
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
    public void onResume() {
        super.onResume();
        mDemoHelper = new DemoHelper(getContext());
        showPendingUI();
        initialize();
    }

    @Override
    public void onPause() {
        mEnrollmentChecked = false;
        super.onPause();
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
                    extractFaceData(requestCode, data);
                } else {
                    prepareUI();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void extractFaceData(final int requestCode, final Intent data) {
        final Bundle extras = data.getExtras();

        new AsyncTask<Void, Void, GKAuthentication.Status>() {
            private IOException ioException;
            private final Bitmap bitmap = (Bitmap) extras.get("data");
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    GKFaces.Template template = mFaces.createTemplateFromBitmap(bitmap);
                    if (requestCode == REQUEST_CAMERA_FOR_AUTHENTICATION) {
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
                } else if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                    if (mIsEnrolled) {
                        showMessage(R.string.authentication_success_message);
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                        return;
                    } else {
                        showMessage(R.string.enrollment_success_prompt_message);
                        mEnroll.setVisibility(View.GONE);
                        mEnroll.setEnabled(false);
                        mAuthenticate.setVisibility(View.VISIBLE);
                        mAuthenticate.setEnabled(true);
                    }
                } else if (status.equals(GKAuthentication.Status.SUCCESS)) {
                    if (mIsEnrolled) {
                        showMessage(R.string.authentication_success_message);
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                        return;
                    } else {
                        showMessage(R.string.enrollment_success_prompt_message);
                        mEnroll.setVisibility(View.GONE);
                        mEnroll.setEnabled(false);
                        mAuthenticate.setVisibility(View.VISIBLE);
                        mAuthenticate.setEnabled(true);
                    }
                } else if (mIsEnrolled) {
                    showMessage(R.string.authentication_failure_message);
                    mEnroll.setVisibility(View.GONE);
                    mEnroll.setEnabled(false);
                    mAuthenticate.setVisibility(View.VISIBLE);
                    mAuthenticate.setEnabled(true);
                } else {
                    showMessage(R.string.enrollment_failure_prompt_message);
                    mEnroll.setVisibility(View.VISIBLE);
                    mEnroll.setEnabled(true);
                    mAuthenticate.setVisibility(View.GONE);
                    mAuthenticate.setEnabled(false);
                }
                prepareUI();
            }
        }.execute();
    }

    private void bypassAuth() {
        mBypassTask = new AsyncTask<Void, Void, GKAuthentication.Status>() {
            private IOException ioException;

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    return mDemoHelper.bypassAuthentication(mCard, mFaces).getStatus();
                } catch (IOException e) {
                    ioException = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                mBypassTask = null;
                if (isCancelled()) {
                    return;
                }
                if (ioException != null) {
                    showRetryConnectDialog();
                } else if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                } else {
                    showMessage(R.string.authentication_error_message);
                    prepareUI();
                }
            }
        }.execute();
    }

    private void initialize() {
        synchronized (mSyncObject) {
            if (!mLicensesReady && !mInitializing) {
                mInitializing = true;
                GKEnvironment.getInstance(getActivity()).initialize(this);
            }
            if (!mEnrollmentChecked) {
                checkForEnrollment();
            }
        }
    }

    private void checkForEnrollment() {
        mEnrollmentChecked = true;
        mCheckEnrollmentTask = new LoadingTask() {
            @Override
            protected Boolean doInBackground(Void... params) {
                GKAuthentication authentication = new GKAuthentication(mCard);
                try {
                    GKAuthentication.ListTemplatesResult result = authentication.listTemplates();
                    return result.getTemplates().size() > 0;
                } catch (IOException e) {
                    mEnrollmentChecked = false;
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

    private void preloadFaces() {
        new AsyncTask<Void, Void, GKFaces>() {
            @Override
            protected GKFaces doInBackground(Void... params) {
                return new GKFaces();
            }

            @Override
            protected void onPostExecute(GKFaces faces) {
                synchronized (mSyncObject) {
                    mFaces = faces;
                }
                checkInitialization();
            }
        }.execute();
    }

    private void checkInitialization() {
        synchronized (mSyncObject) {
            if (mLicensesReady && mFaces != null && mEnrollmentChecked) {
                prepareUI();
            }
        }
    }

    private void showPendingUI() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuthenticate.setVisibility(View.GONE);
        mEnroll.setVisibility(View.GONE);
        mDemoSetup.setEnabled(false);
        mBypassAuth.setEnabled(false);
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

    protected void showMessage(int messageResource) {
        Log.i(TAG, getString(messageResource));
        Toast toast = Toast.makeText(getContext(), messageResource, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onLicensesObtained() {
        synchronized (mSyncObject) {
            mLicensesReady = true;
            mInitializing = false;
        }
        preloadFaces();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }
    }
}
