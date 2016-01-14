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
        NOT_ENROLLED,
        ENROLLED
    }

    protected final Object mSyncObject = new Object();

    private AuthState mAuthState = AuthState.UNCHECKED;

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
        showPendingUI();
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
        boolean uncheckedEnrollment = mAuthState.equals(AuthState.UNCHECKED) || mAuthState.equals(AuthState.CHECKING);
        if (uncheckedEnrollment || !biometricsAvailable()) {
            showPendingUI();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mDemoSetup.setEnabled(cardIsAvailable());
            updateAuthButtons();
        }
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
        }
        initialize();
    }

    private void initialize() {
        synchronized (mSyncObject) {
            if (cardIsAvailable()) {
                if (mAuthState.equals(AuthState.UNCHECKED)) {
                    checkForEnrollment();
                }
            }
            updateUI();
        }
    }

    private void checkForEnrollment() {
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
                    synchronized (mSyncObject) {
                        mAuthState = AuthState.UNCHECKED;
                    }
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean templateExists) {
                synchronized (mSyncObject) {
                    mAuthState = templateExists ? AuthState.ENROLLED : AuthState.NOT_ENROLLED;
                }
                initialize();
            }
        }.execute();
    }

    private void bypassAuth() {
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
                updateUI();
            }
        }.execute();
    }

    private void disableActions() {
        mEnroll.setEnabled(false);
        mAuthenticate.setEnabled(false);
        mDemoSetup.setEnabled(false);
        mBypassAuth.setEnabled(false);
    }

    private void updateAuthButtons() {
        boolean authActionsAvailable = biometricsAvailable();
        if (mAuthState == AuthState.ENROLLED) {
            mAuthenticate.setVisibility(View.VISIBLE);
            mAuthenticate.setEnabled(cardIsAvailable() && authActionsAvailable);
            mEnroll.setVisibility(View.GONE);
            mEnroll.setEnabled(false);
            mBypassAuth.setEnabled(cardIsAvailable() && authActionsAvailable);
        } else if (mAuthState == AuthState.NOT_ENROLLED) {
            mAuthenticate.setVisibility(View.GONE);
            mAuthenticate.setEnabled(false);
            mEnroll.setVisibility(View.VISIBLE);
            mEnroll.setEnabled(cardIsAvailable() && authActionsAvailable);
            mBypassAuth.setEnabled(false);
        }
    }

    private boolean biometricsAvailable() {
        return mFaces != null;
    }
}
