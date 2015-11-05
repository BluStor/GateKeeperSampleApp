package co.blustor.gatekeeper.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.neurotec.biometrics.NSubject;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.authentication.LocalFaceAuthenticator;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class AuthenticationActivity extends FaceAuthActivity {
    public static final String TAG = AuthenticationActivity.class.getSimpleName();

    private LocalFaceAuthenticator mLocalFaceAuthenticator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCaptureButtonText(R.string.authenticate);
        Datastore datastore = DroidDatastore.getInstance(this);
        mLocalFaceAuthenticator = new LocalFaceAuthenticator(datastore);
    }

    @Override
    protected void onCaptureSuccess(NSubject subject) {
        if (mLocalFaceAuthenticator.authenticate(subject)) {
            showMessage(R.string.authentication_result_success);
            startActivity(new Intent(AuthenticationActivity.this, AppLauncherActivity.class));
            finish();
        } else {
            showMessage(R.string.authentication_result_failure);
            startCapture();
        }
    }

    @Override
    protected void onCaptureFailure(Throwable e) {
        showFailurePrompt();
    }

    private void showFailurePrompt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.authenticate_failure_prompt_title);
        builder.setMessage(R.string.authenticate_failure_prompt_message);
        builder.setPositiveButton(R.string.authenticate_failure_prompt_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCapture();
            }
        });
        showPrompt(builder);
    }
}
