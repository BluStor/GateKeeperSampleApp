package co.blustor.gatekeeperdemo.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.scopes.DemoAuthentication;

public class DemoFragment extends CardFragment {
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

    protected abstract class AuthTask extends AsyncTask<Void, Void, GKAuthentication.Status> {
        protected final DemoAuthentication auth = new DemoAuthentication(mCard, getContext());
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
