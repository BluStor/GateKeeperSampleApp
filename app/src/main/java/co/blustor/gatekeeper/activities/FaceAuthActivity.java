package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.view.NFaceView;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.biometrics.FaceCapture;
import co.blustor.gatekeeper.fragments.PINEntryDialog;

public abstract class FaceAuthActivity extends Activity implements FaceCapture.Listener, PINEntryDialog.Listener {
    public static final String TAG = FaceAuthActivity.class.getSimpleName();

    private NFaceView mFaceView;
    private Button mCaptureButton;
    private Button mPinToggleButton;

    private boolean mCaptureActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);
        initializeViews();
        startCapture();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCapture();
    }

    protected void enablePINEntry() {
        mPinToggleButton.setVisibility(View.VISIBLE);
    }

    protected void setCaptureButtonText(int textResource) {
        mCaptureButton.setText(textResource);
    }

    protected void setCaptureButtonEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaptureButton.setEnabled(enabled);
            }
        });
    }

    protected void startCapture() {
        final FaceCapture faceCapture = FaceCapture.getInstance();
        faceCapture.setListener(this);
        mCaptureActive = true;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                faceCapture.start();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mFaceView.setFace(faceCapture.getFace());
                setCaptureButtonEnabled(true);
                showMessage(R.string.turn_camera_to_face);
            }
        }.execute();
    }

    protected void completeCapture() {
        if (mCaptureActive) {
            Log.i(TAG, "Completing Capture");
            FaceCapture.getInstance().complete();
        }
    }

    protected void cancelCapture() {
        if (mCaptureActive) {
            Log.i(TAG, "Canceling Capture");
            FaceCapture faceCapture = FaceCapture.getInstance();
            faceCapture.removeListener(this);
            faceCapture.stop();
            mCaptureActive = false;
        }
    }

    protected void showMessage(final int messageResource) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, getString(messageResource));
                Toast toast = Toast.makeText(context, messageResource, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    protected void showPrompt(final AlertDialog.Builder dialogBuilder) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogBuilder.create().show();
            }
        });
    }

    @Override
    public void onCaptureComplete(NSubject subject) {
        mCaptureActive = false;
        FaceCapture.getInstance().removeListener(this);
    }

    @Override
    public void onCaptureIncomplete() {
        showMessage(R.string.bio_status_not_ok);
        startCapture();
    }

    @Override
    public void onCaptureFailure() {
        mCaptureActive = false;
        FaceCapture.getInstance().removeListener(this);
        showFailurePrompt();
    }

    protected abstract void showFailurePrompt();

    private void initializeViews() {
        mFaceView = (NFaceView) findViewById(R.id.camera_view);
        mCaptureButton = (Button) findViewById(R.id.capture);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCaptureButtonEnabled(false);
                completeCapture();
            }
        });
        mPinToggleButton = (Button) findViewById(R.id.toggle_pin_entry);
        mPinToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPinEntryDialog();
            }
        });
    }

    private void showPinEntryDialog() {
        PINEntryDialog pinEntryDialog = new PINEntryDialog();
        pinEntryDialog.setListener(this);
        pinEntryDialog.show(getFragmentManager(), PINEntryDialog.TAG);
    }

    @Override
    public void onSubmitPIN(String pin) {
        Log.i(TAG, "PIN Entered: " + pin);
    }
}
