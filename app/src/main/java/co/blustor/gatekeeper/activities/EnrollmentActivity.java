package co.blustor.gatekeeper.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.neurotec.biometrics.NSubject;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.demo.Application;

public class EnrollmentActivity extends FaceAuthActivity {
    public static final String TAG = EnrollmentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCaptureButtonText(R.string.capture);
    }

    @Override
    public void onCaptureComplete(NSubject subject) {
        super.onCaptureComplete(subject);
        Authentication authentication = Application.getAuthentication();
        Authentication.AuthResult result = authentication.enrollWithFace(subject);
        switch (result.status) {
            case SUCCESS:
                showSuccessPrompt();
                break;
            default:
                showFailurePrompt();
        }
    }

    private void showSuccessPrompt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enroll_success_prompt_title);
        builder.setMessage(R.string.enroll_success_prompt_message);
        builder.setPositiveButton(R.string.enroll_success_prompt_okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(EnrollmentActivity.this, AuthenticationActivity.class));
                finish();
            }
        });
        showPrompt(builder);
    }

    @Override
    protected void showFailurePrompt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enroll_failure_prompt_title);
        builder.setMessage(R.string.enroll_failure_prompt_message);
        builder.setPositiveButton(R.string.enroll_failure_prompt_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCapture();
            }
        });
        showPrompt(builder);
    }
}
