package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.FaceCaptureFragment;

public class FaceCaptureActivity extends CardActivity implements FaceCaptureFragment.OnTemplateCapturedListener {
    private static final String IS_AUTHENTICATING = "isAuthenticating";
    private static final String IS_UPDATING = "isUpdating";
    private boolean mIsAuthenticating;
    private boolean mIsUpdating;

    public static Intent createIntent(Context context, boolean isAuthenticating, boolean isUpdating) {
        Intent intent = new Intent(context, FaceCaptureActivity.class);
        intent.putExtra(IS_AUTHENTICATING, isAuthenticating);
        intent.putExtra(IS_UPDATING, isUpdating);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        getSupportActionBar().hide();

        mIsAuthenticating = getIntent().getBooleanExtra(IS_AUTHENTICATING, false);
        mIsUpdating = getIntent().getBooleanExtra(IS_UPDATING, false);
    }

    @Override
    protected void setInitialFragment() {
        pushFragment(new FaceCaptureFragment(), FaceCaptureFragment.TAG);
    }

    @Override
    public void onTemplateCaptured(final GKFaces.Template template) {
        new AsyncTask<Void, Void, GKAuthentication.Status>() {
            public IOException ioException;
            private final GKAuthentication auth = new GKAuthentication(mCard);

            @Override
            protected GKAuthentication.Status doInBackground(Void... params) {
                try {
                    if (mIsAuthenticating) {
                        return auth.signInWithFace(template).getStatus();
                    } else {
                        return auth.enrollWithFace(template).getStatus();
                    }
                } catch (IOException e) {
                    ioException = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                boolean restartAuth = true;
                if (ioException == null) {
                    if (status.equals(GKAuthentication.Status.SIGNED_IN)) {
                        showMessage(R.string.authentication_success_message);
                        restartAuth = false;
                    } else if (status.equals(GKAuthentication.Status.TEMPLATE_ADDED) && mIsUpdating) {
                        showMessage(R.string.update_template_prompt_message);
                    } else if (status.equals(GKAuthentication.Status.TEMPLATE_ADDED)) {
                        showMessage(R.string.enrollment_success_prompt_message);
                    } else if (mIsAuthenticating) {
                        showMessage(R.string.authentication_failure_message);
                    } else {
                        showMessage(R.string.enrollment_failure_prompt_message);
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(RESTART_AUTH, restartAuth);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }.execute();
    }
}
