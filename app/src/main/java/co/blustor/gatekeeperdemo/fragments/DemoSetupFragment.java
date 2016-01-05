package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.neurotec.biometrics.NSubject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.biometrics.GKFaceExtractor;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.scopes.DemoAuthentication;

public class DemoSetupFragment extends DemoFragment {
    public static final String TAG = DemoSetupFragment.class.getSimpleName();

    private static final int REQUEST_CAMERA_FOR_ENROLLMENT = 1;

    private final Object mSyncObject = new Object();

    private boolean mEnrollmentSynced;
    private boolean mCardReady;
    private boolean mHasCapturedTemplate;
    private boolean mHasTestTemplate;

    private Button mCaptureNewTemplate;
    private Button mRemoveCapturedTemplate;
    private Button mAddTestTemplate;
    private Button mRemoveTestTemplate;

    private GKFaceExtractor mFaceExtractor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo_setup, container, false);
        initializeAuthActions(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        syncEnrollmentState();
        preloadFaceExtractor();
    }

    private void initializeAuthActions(View view) {
        mCaptureNewTemplate = (Button) view.findViewById(R.id.capture_new_template);
        mCaptureNewTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFacePhoto();
            }
        });
        mRemoveCapturedTemplate = (Button) view.findViewById(R.id.remove_captured_template);
        mRemoveCapturedTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCapturedTemplate();
            }
        });
        mAddTestTemplate = (Button) view.findViewById(R.id.add_test_template);
        mAddTestTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTestTemplate();
            }
        });
        mRemoveTestTemplate = (Button) view.findViewById(R.id.remove_test_template);
        mRemoveTestTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTestTemplate();
            }
        });
    }

    private void checkInitialization() {
        synchronized (mSyncObject) {
            if (mEnrollmentSynced && mCardReady && mFaceExtractor != null) {
                updateUI();
            }
        }
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

    private void syncEnrollmentState() {
        synchronized (mSyncObject) {
            mHasTestTemplate = false;
            mHasCapturedTemplate = false;
        }
        new AsyncTask<Void, Void, List<Object>>() {
            private IOException ioException;
            private final DemoAuthentication auth = new DemoAuthentication(mCard, getContext());

            @Override
            protected List<Object> doInBackground(Void... params) {
                try {
                    return auth.listTemplates();
                } catch (IOException e) {
                    ioException = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Object> objects) {
                if (ioException != null) {
                    showRetryConnectDialog();
                } else {
                    mEnrollmentSynced = true;
                    mHasTestTemplate = objects.size() > 0;
                    for (Object object : objects) {
                        if (object.equals("face000")) {
                            mHasCapturedTemplate = true;
                        }
                    }
                }
                ensureCardAccess();
            }
        }.execute();
    }

    private void ensureCardAccess() {
        if (mHasTestTemplate) {
            new AuthTask() {
                @Override
                protected GKAuthentication.Status perform() throws IOException {
                    String templatePath = getAbsolutePath("GoodTemplate.dat");
                    NSubject subject = NSubject.fromFile(templatePath);
                    return auth.signInWithFace(subject);
                }

                @Override
                protected void onPostExecute(GKAuthentication.Status status) {
                    if (mIOException != null) {
                        showRetryConnectDialog();
                    } else {
                        mCardReady = true;
                        checkInitialization();
                    }
                }
            }.execute();
        } else {
            mCardReady = true;
            checkInitialization();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA_FOR_ENROLLMENT:
                if (resultCode == Activity.RESULT_OK) {
                    extractFaceData(data);
                } else {
                    updateUI();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void extractFaceData(final Intent data) {
        final Bundle extras = data.getExtras();

        disableUI();
        new AuthTask() {
            private final Bitmap bitmap = (Bitmap) extras.get("data");

            @Override
            protected GKAuthentication.Status perform() throws IOException {
                try {
                    NSubject subject = mFaceExtractor.getSubjectFromBitmap(bitmap);
                    if (subject != null) {
                        return auth.enrollWithFace(subject);
                    } else {
                        return GKAuthentication.Status.BAD_TEMPLATE;
                    }
                } finally {
                    bitmap.recycle();
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasCapturedTemplate = true;
                }
                updateUI();
            }
        }.execute();
    }

    private void requestFacePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_FOR_ENROLLMENT);
        }
    }

    private void deleteCapturedTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.revokeFace();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasCapturedTemplate = false;
                }
                updateUI();
            }
        }.execute();
    }

    private void addTestTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.enrollWithTestFace();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasTestTemplate = true;
                }
                updateUI();
            }
        }.execute();
    }

    private void deleteTestTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.revokeTestFace();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasTestTemplate = false;
                }
                updateUI();
            }
        }.execute();
    }

    @NonNull
    private String getAbsolutePath(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, filename).getAbsolutePath();
    }

    private void updateUI() {
        mCaptureNewTemplate.setEnabled(!mHasCapturedTemplate && mHasTestTemplate);
        mRemoveCapturedTemplate.setEnabled(mHasCapturedTemplate && mHasTestTemplate);
        mAddTestTemplate.setEnabled(!mHasTestTemplate);
        mRemoveTestTemplate.setEnabled(!mHasCapturedTemplate && mHasTestTemplate);
    }

    private void disableUI() {
        mCaptureNewTemplate.setEnabled(false);
        mRemoveCapturedTemplate.setEnabled(false);
        mAddTestTemplate.setEnabled(false);
        mRemoveTestTemplate.setEnabled(false);
    }
}
