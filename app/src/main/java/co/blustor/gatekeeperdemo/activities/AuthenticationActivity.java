package co.blustor.gatekeeperdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeper.authentication.GKCardAuthentication;
import co.blustor.gatekeeperdemo.Application;

public class AuthenticationActivity extends FaceAuthActivity {
    public static final String TAG = AuthenticationActivity.class.getSimpleName();

    private GKCardAuthentication mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCaptureButtonText(R.string.authenticate);
        mAuthentication = Application.getAuthentication();
        enablePINEntry();
    }

    @Override
    public void onCaptureComplete(NSubject subject) {
        super.onCaptureComplete(subject);
        try {
            GKCardAuthentication.Status status = mAuthentication.signInWithFace(subject);
            switch (status) {
                case SUCCESS:
                    startActivity(new Intent(AuthenticationActivity.this, AppLauncherActivity.class));
                    finish();
                    break;
                default:
                    showMessage(R.string.authentication_result_failure);
                    startCapture();
            }
        } catch (IOException e) {
            Log.e(TAG, "Communication error with GKCard", e);
        }
    }

    @Override
    protected void showFailurePrompt() {
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

    @Override
    public void onSubmitPIN(String pin) {
        Log.i(TAG, "PIN Entered: " + pin);
        if (pin.equals("1234")) {
            cancelCapture();
            startActivity(new Intent(AuthenticationActivity.this, AppLauncherActivity.class));
            finish();
        } else {
            showMessage(R.string.invalid_pin);
        }
    }
}
