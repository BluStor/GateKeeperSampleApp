package co.blustor.gatekeeperdemo.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.DemoSetupActivity;

public class AuthFragment extends DemoFragment {
    public static final String TAG = AuthFragment.class.getSimpleName();

    enum AuthState {
        UNCHECKED,
        CHECKING,
        CHECKED
    }

    protected final Object mSyncObject = new Object();

    private AuthState mAuthState = AuthState.UNCHECKED;

    private ProgressBar mProgressBar;

    private Button mEnroll;
    private Button mAuthenticate;
    private Button mDemoSetup;
    private Button mBypassAuth;

    private boolean mFragmentBusy;
    private boolean mIsEnrolled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mEnroll = (Button) view.findViewById(R.id.enroll);
        mEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableActions();
                getCardActivity().startEnrollment();
            }
        });
        mAuthenticate = (Button) view.findViewById(R.id.authenticate);
        mAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableActions();
                getCardActivity().startAuthentication();
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
                disableActions();
                bypassAuth();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
    }

    @Override
    public void onDestroy() {
        mAuthState = AuthState.UNCHECKED;
        super.onDestroy();
    }

    @Override
    public void setFaces(GKFaces faces) {
        super.setFaces(faces);
        initialize();
    }

    @Override
    public void updateUI() {
        boolean cardIsBusy = mCardState.equals(GKCard.ConnectionState.TRANSFERRING) || mCardState.equals(GKCard.ConnectionState.CONNECTING);
        if (cardIsBusy || !biometricsAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mDemoSetup.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mDemoSetup.setEnabled(!isBusy() && cardIsAvailable());
        }
        updateAuthButtons();
    }

    @Override
    public void showPendingUI() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuthenticate.setVisibility(View.GONE);
        mEnroll.setVisibility(View.GONE);
        mDemoSetup.setEnabled(false);
        mBypassAuth.setEnabled(false);
    }

    @Override
    protected void onCardStateChanged(GKCard.ConnectionState state) {
        synchronized (mSyncObject) {
            super.onCardStateChanged(state);
            if (state.equals(GKCard.ConnectionState.DISCONNECTED)) {
                mAuthState = AuthState.UNCHECKED;
            }
        }
        initialize();
    }

    protected boolean isBusy() {
        return mFragmentBusy;
    }

    private void initialize() {
        synchronized (mSyncObject) {
            if (cardIsAvailable() && mAuthState.equals(AuthState.UNCHECKED)) {
                setFragmentBusy(true);
                checkForEnrollment();
            } else {
                boolean checking = mAuthState.equals(AuthState.CHECKING);
                boolean cardBusy = mCardState.equals(GKCard.ConnectionState.TRANSFERRING);
                setFragmentBusy(checking || cardBusy);
            }
            updateUI();
        }
    }

    private void checkForEnrollment() {
        setFragmentBusy(true);
        synchronized (mSyncObject) {
            mAuthState = AuthState.CHECKING;
        }
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                GKAuthentication authentication = new GKAuthentication(mCard);
                try {
                    GKAuthentication.ListTemplatesResult result = authentication.listTemplates();
                    return result.getTemplates().size() > 0;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean templateExists) {
                synchronized (mSyncObject) {
                    mAuthState = AuthState.CHECKED;
                    mIsEnrolled = templateExists;
                }
                initialize();
            }
        }.execute();
    }

    private void bypassAuth() {
        setFragmentBusy(true);
        new AsyncTask<Void, Void, GKAuthentication.Status>() {
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
                if (ioException != null) {
                } else if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                    getCardActivity().startMainActivity();
                    return;
                } else {
                    showMessage(R.string.authentication_error_message);
                }
                setFragmentBusy(false);
                updateUI();
            }
        }.execute();
    }

    private void setFragmentBusy(boolean busy) {
        synchronized (mSyncObject) {
            mFragmentBusy = busy;
        }
    }

    private void disableActions() {
        mEnroll.setEnabled(false);
        mAuthenticate.setEnabled(false);
        mDemoSetup.setEnabled(false);
        mBypassAuth.setEnabled(false);
    }

    private void updateAuthButtons() {
        boolean authActionsEnabled = !isBusy() && biometricsAvailable() && cardIsAvailable();
        int enabledVisibility = authActionsEnabled ? View.VISIBLE : View.GONE;
        if (mAuthState.equals(AuthState.CHECKED)) {
            mAuthenticate.setVisibility(mIsEnrolled ? enabledVisibility : View.GONE);
            mAuthenticate.setEnabled(mIsEnrolled && authActionsEnabled);
            mEnroll.setVisibility(!mIsEnrolled ? enabledVisibility : View.GONE);
            mEnroll.setEnabled(!mIsEnrolled && authActionsEnabled);
            mBypassAuth.setEnabled(mIsEnrolled && authActionsEnabled);
        } else {
            mAuthenticate.setVisibility(View.GONE);
            mAuthenticate.setEnabled(false);
            mEnroll.setVisibility(View.GONE);
            mEnroll.setEnabled(false);
            mBypassAuth.setEnabled(false);
        }
    }

    private boolean biometricsAvailable() {
        return mFaces != null;
    }
}
