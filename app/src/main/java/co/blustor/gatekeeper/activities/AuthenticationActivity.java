package co.blustor.gatekeeper.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.neurotec.biometrics.NSubject;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.demo.Application;

public class AuthenticationActivity extends FaceAuthActivity {
    public static final String TAG = AuthenticationActivity.class.getSimpleName();

    private Authentication mAuthentication;

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
        if (mAuthentication.signInWithFace(subject)) {
            startActivity(new Intent(AuthenticationActivity.this, AppLauncherActivity.class));
            finish();
        } else {
            showMessage(R.string.authentication_result_failure);
            startCapture();
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
