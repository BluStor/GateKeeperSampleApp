package co.blustor.gatekeeperdemo.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.utils.DemoHelper;

public class DemoFragment extends CardFragment {
    protected boolean mHasDemoTemplate;

    protected DemoHelper mDemoHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDemoHelper = new DemoHelper(context);
    }

    @Override
    public void updateUI() {
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
                if (mIOException == null && status.equals(GKAuthentication.Status.TEMPLATE_ADDED)) {
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
