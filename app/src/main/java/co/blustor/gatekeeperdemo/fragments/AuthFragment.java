package co.blustor.gatekeeperdemo.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;

public class AuthFragment extends DemoFragment {
    public static final String TAG = AuthFragment.class.getSimpleName();
    private Menu mMenu;

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
                showPendingUI();
                try {
                    mDemoHelper.bypassAuthentication(mCard, mFaces);
                    getCardActivity().startEnrollment();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to bypass authentication", e);
                }
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
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_auth, menu);
        mMenu = menu;
        MenuItem item = menu.findItem(R.id.connection_status);
        item.setEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem capturedTemplateItem = menu.findItem(R.id.remove_captured_template);
        MenuItem bypassItem = menu.findItem(R.id.bypass);
        if (mFragmentBusy) {
            capturedTemplateItem.setEnabled(false);
            bypassItem.setEnabled(false);
        } else {
            capturedTemplateItem.setEnabled(mIsEnrolled);
            bypassItem.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bypass:
                bypassAuth();
                return true;
            case R.id.remove_captured_template:
                mDemoHelper.removeFaceTemplate(mCard);
                checkForEnrollment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        synchronized (mSyncObject) {
            mAuthState = AuthState.UNCHECKED;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        synchronized (mSyncObject) {
            mAuthState = AuthState.UNCHECKED;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        synchronized (mSyncObject) {
            mAuthState = AuthState.UNCHECKED;
        }
        super.onDestroy();
    }

    @Override
    public void setFaces(GKFaces faces) {
        synchronized (mSyncObject) {
            super.setFaces(faces);
        }
        initialize();
    }

    @Override
    public void updateUI() {
        boolean cardIsBusy = mCardState.equals(GKCard.ConnectionState.TRANSFERRING) || mCardState.equals(GKCard.ConnectionState.CONNECTING);
        if (cardIsBusy || !biometricsAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
        updateAuthButtons();
    }

    @Override
    public void showPendingUI() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuthenticate.setVisibility(View.GONE);
        mEnroll.setVisibility(View.GONE);
    }

    @Override
    protected void onCardStateChanged(GKCard.ConnectionState state) {
        synchronized (mSyncObject) {
            super.onCardStateChanged(state);
            if (state.equals(GKCard.ConnectionState.DISCONNECTED)) {
                mAuthState = AuthState.UNCHECKED;
            }

            updateMenu(state);
        }
        initialize();
    }

    private void updateMenu(GKCard.ConnectionState state) {
        if (mMenu != null) {
            if (state.equals(GKCard.ConnectionState.TRANSFERRING)) {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
            } else if (state.equals(GKCard.ConnectionState.CONNECTED)) {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_black_24dp);
            } else {
                mMenu.findItem(R.id.connection_status).setIcon(R.drawable.ic_bluetooth_disabled_black_24dp);
            }
        }
    }

    protected boolean isBusy() {
        return mFragmentBusy;
    }

    private void initialize() {
        synchronized (mSyncObject) {
            boolean facesAvailable = mFaces != null;
            if (cardIsAvailable() && facesAvailable && mAuthState.equals(AuthState.UNCHECKED)) {
                checkForEnrollment();
            } else {
                boolean checking = mAuthState.equals(AuthState.CHECKING);
                boolean cardBusy = mCardState.equals(GKCard.ConnectionState.TRANSFERRING);
                setFragmentBusy(checking || cardBusy || !facesAvailable);
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
                try {
                    return isEnrolled();
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

    @NonNull
    private Boolean isEnrolled() throws IOException {
        return mDemoHelper.cardHasCapturedEnrollment(mCard, mFaces);
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
    }

    private void updateAuthButtons() {
        boolean authActionsEnabled = !isBusy() && biometricsAvailable() && cardIsAvailable();
        int enabledVisibility = authActionsEnabled ? View.VISIBLE : View.GONE;
        if (mAuthState.equals(AuthState.CHECKED)) {
            mAuthenticate.setVisibility(mIsEnrolled ? enabledVisibility : View.GONE);
            mAuthenticate.setEnabled(mIsEnrolled && authActionsEnabled);
            mEnroll.setVisibility(!mIsEnrolled ? enabledVisibility : View.GONE);
            mEnroll.setEnabled(!mIsEnrolled && authActionsEnabled);
        } else {
            mAuthenticate.setVisibility(View.GONE);
            mAuthenticate.setEnabled(false);
            mEnroll.setVisibility(View.GONE);
            mEnroll.setEnabled(false);
        }
    }

    private boolean biometricsAvailable() {
        return mFaces != null;
    }
}
