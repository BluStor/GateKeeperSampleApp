package co.blustor.gatekeeperdemo.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKEnvironment;
import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.utils.DemoHelper;

public class DemoFragment extends CardFragment implements GKEnvironment.InitializationListener {
    protected final Object mSyncObject = new Object();

    protected boolean mInitializing;
    protected boolean mLicensesReady;
    protected boolean mHasDemoTemplate;

    protected GKFaces mFaces;
    protected DemoHelper mDemoHelper;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preloadFaces();
        mDemoHelper = new DemoHelper(getContext());
    }

    @Override
    public void onLicensesObtained() {
        synchronized (mSyncObject) {
            mLicensesReady = true;
            mInitializing = false;
        }
        preloadFaces();
    }

    protected void reportString(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    protected void reportStatus(GKAuthentication.Status status) {
        Toast.makeText(getContext(), status.name(), Toast.LENGTH_LONG).show();
    }

    protected void reportResponse(GKCard.Response response) {
        Toast.makeText(getContext(), response.getStatusMessage(), Toast.LENGTH_LONG).show();
    }

    protected void reportException(IOException e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    protected void checkInitialization() {
        synchronized (mSyncObject) {
            if (mFaces != null) {
                updateUI();
            }
        }
    }

    protected void updateUI() {
    }

    protected void disableUI() {
    }

    protected void addDemoTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return mDemoHelper.addDemoTemplate(mCard, mFaces).getStatus();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasDemoTemplate = true;
                }
                updateUI();
            }
        }.execute();
    }

    protected void deleteDemoTemplate() {
        disableUI();
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return mDemoHelper.removeDemoTemplate(mCard).getStatus();
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mIOException == null && status.equals(GKAuthentication.Status.SUCCESS)) {
                    mHasDemoTemplate = false;
                }
                updateUI();
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

    protected abstract class AuthTask extends AsyncTask<Void, Void, GKAuthentication.Status> {
        protected final GKAuthentication auth = new GKAuthentication(mCard);
        protected IOException mIOException;

        protected abstract GKAuthentication.Status perform() throws IOException;

        @Override
        protected GKAuthentication.Status doInBackground(Void... params) {
            try {
                return perform();
            } catch (IOException e) {
                mIOException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(GKAuthentication.Status status) {
            super.onPostExecute(status);
            if (status != null) {
                reportStatus(status);
            }
            if (mIOException != null) {
                reportException(mIOException);
            }
        }
    }
}
