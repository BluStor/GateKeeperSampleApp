package co.blustor.gatekeeperdemo.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;

public class DemoSetupFragment extends DemoFragment {
    public static final String TAG = DemoSetupFragment.class.getSimpleName();

    enum AuthState {
        UNCHECKED,
        CHECKING,
        CHECKED
    }

    protected final Object mSyncObject = new Object();

    private AuthState mAuthState = AuthState.UNCHECKED;

    protected boolean mAuthenticated;
    protected boolean mHasCapturedTemplate;

    private Button mCaptureNewTemplate;
    private Button mRemoveCapturedTemplate;
    private Button mAddDemoTemplate;
    private Button mRemoveDemoTemplate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo_setup, container, false);
        mCaptureNewTemplate = (Button) view.findViewById(R.id.capture_new_template);
        mCaptureNewTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCardActivity().startEnrollment();
            }
        });
        mRemoveCapturedTemplate = (Button) view.findViewById(R.id.remove_captured_template);
        mRemoveCapturedTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCapturedTemplate();
            }
        });
        mAddDemoTemplate = (Button) view.findViewById(R.id.add_demo_template);
        mAddDemoTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDemoTemplate();
            }
        });
        mRemoveDemoTemplate = (Button) view.findViewById(R.id.remove_demo_template);
        mRemoveDemoTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDemoTemplate();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        disableUI();
        checkInitialization();
    }

    @Override
    public void onPause() {
        setAuthState(AuthState.UNCHECKED);
        super.onPause();
    }

    @Override
    public void setFaces(GKFaces faces) {
        synchronized (mSyncObject) {
            super.setFaces(faces);
        }
        checkInitialization();
    }

    @Override
    protected void setCardAvailable(boolean available) {
        synchronized (mSyncObject) {
            super.setCardAvailable(available);
        }
        if (available) {
            checkInitialization();
        }
        updateUI();
    }

    @Override
    public void showPendingUI() {
        super.showPendingUI();
        disableUI();
    }

    @Override
    public void onCardAccessUpdated() {
        super.onCardAccessUpdated();
        setAuthState(AuthState.UNCHECKED);
        checkInitialization();
    }

    protected void checkInitialization() {
        synchronized (mSyncObject) {
            if (mFaces == null || mAuthState == AuthState.CHECKING) {
                return;
            }
            if (mAuthenticated) {
                updateUI();
            } else {
                syncEnrollmentState();
            }
        }
    }

    @Override
    public void updateUI() {
        mCaptureNewTemplate.setEnabled(!mHasCapturedTemplate && mHasDemoTemplate);
        mRemoveCapturedTemplate.setEnabled(mHasCapturedTemplate && mHasDemoTemplate);
        mAddDemoTemplate.setEnabled(!mHasDemoTemplate);
        mRemoveDemoTemplate.setEnabled(!mHasCapturedTemplate && mHasDemoTemplate);
    }

    @Override
    protected void disableUI() {
        mCaptureNewTemplate.setEnabled(false);
        mRemoveCapturedTemplate.setEnabled(false);
        mAddDemoTemplate.setEnabled(false);
        mRemoveDemoTemplate.setEnabled(false);
    }

    private void syncEnrollmentState() {
        disableUI();
        setAuthState(AuthState.CHECKING);
        synchronized (mSyncObject) {
            mHasDemoTemplate = false;
            mHasCapturedTemplate = false;
        }

        new AsyncTask<Void, Void, GKAuthentication.ListTemplatesResult>() {
            private IOException ioException;
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.ListTemplatesResult doInBackground(Void... params) {
                try {
                    GKAuthentication.ListTemplatesResult result = auth.listTemplates();
                    if (result.getStatus() == GKAuthentication.Status.UNAUTHORIZED) {
                        // TODO: If this does not succeed, we have a captured template and no demo template
                        // ^^^ THIS IS VERY DANGEROUS ^^^
                        mDemoHelper.bypassAuthentication(mCard, mFaces);
                        setAuthenticated(true);
                        return auth.listTemplates();
                    } else if (result.equals(GKAuthentication.Status.SUCCESS)) {
                        setAuthenticated(result.getTemplates().size() > 0);
                    }
                    return result;
                } catch (IOException e) {
                    ioException = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.ListTemplatesResult result) {
                if (ioException != null) {
                    setAuthState(AuthState.UNCHECKED);
                } else {
                    List<Object> templates = result.getTemplates();
                    setAuthState(AuthState.CHECKED);
                    synchronized (mSyncObject) {
                        // This assumes that we always have a Demo Template before we can capture a Template
                        mHasDemoTemplate = templates.size() > 0;
                    }
                    for (Object template : templates) {
                        if (template.equals("face000")) {
                            synchronized (mSyncObject) {
                                mHasCapturedTemplate = true;
                            }
                        }
                    }
                }
                updateUI();
            }
        }.execute();
    }

    private void deleteCapturedTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.revokeFace().getStatus();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                syncEnrollmentState();
            }
        }.execute();
    }

    private void setAuthState(AuthState state) {
        synchronized (mSyncObject) {
            mAuthState = state;
        }
    }

    private void setAuthenticated(boolean authenticated) {
        synchronized (mSyncObject) {
            mAuthenticated = authenticated;
        }
    }
}
