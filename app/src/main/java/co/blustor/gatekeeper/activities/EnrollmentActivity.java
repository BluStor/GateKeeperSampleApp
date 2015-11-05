package co.blustor.gatekeeper.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class EnrollmentActivity extends FaceAuthActivity {
    public static final String TAG = EnrollmentActivity.class.getSimpleName();

    private Datastore mDatastore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCaptureButtonText(R.string.capture);
        mDatastore = DroidDatastore.getInstance(this);
    }

    @Override
    protected void onCaptureSuccess(NSubject subject) {
        try {
            mDatastore.storeTemplate(subject);
            showSuccessPrompt();
        } catch (IOException e) {
            Log.e(TAG, "Something exploded", e);
            showFailurePrompt();
        }
    }

    @Override
    protected void onCaptureFailure(Throwable e) {
        showFailurePrompt();
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

    private void showFailurePrompt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enroll_failure_prompt_title);
        builder.setMessage(R.string.enroll_failure_prompt_message);
        builder.setPositiveButton(R.string.enroll_failure_prompt_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCapturing();
            }
        });
        showPrompt(builder);
    }
}
