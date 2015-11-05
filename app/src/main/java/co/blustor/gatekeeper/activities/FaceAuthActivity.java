package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.util.concurrent.CompletionHandler;

import java.util.EnumSet;

import co.blustor.gatekeeper.R;

public abstract class FaceAuthActivity extends Activity {
    public static final String TAG = FaceAuthActivity.class.getSimpleName();

    private NFaceView mFaceView;
    private Button mCaptureButton;

    private NBiometricClient mBiometricClient;

    private boolean mCapturing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);
        initializeViews();
        initializeClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCapture();
        if (mBiometricClient != null) {
            mBiometricClient.cancel();
            mBiometricClient.dispose();
            mBiometricClient = null;
        }
    }

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
    }

    protected void onReadyToCapture() {
        startCapture();
    }

    private void initializeClient() {
        mBiometricClient = new NBiometricClient();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mBiometricClient.setUseDeviceManager(true);
                NDeviceManager deviceManager = mBiometricClient.getDeviceManager();
                deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
                mBiometricClient.initialize();
                onReadyToCapture();
                return null;
            }
        }.execute();
    }

    protected void setCaptureButtonText(int textResource) {
        mCaptureButton.setText(textResource);
    }

    protected void startCapture() {
        Log.i(TAG, "Starting Capture");
        mCapturing = true;
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        mFaceView.setFace(face);
        subject.getFaces().add(face);

        NCamera camera = (NCamera) connectCamera(mBiometricClient.getDeviceManager());
        mBiometricClient.setFaceCaptureDevice(camera);
        mBiometricClient.capture(subject, subject, createCompletionHandler());
        setCaptureButtonEnabled(true);
        showMessage(R.string.turn_camera_to_face);
    }

    protected void completeCapture() {
        Log.i(TAG, "Completing Capture");
        mBiometricClient.force();
    }

    private void cancelCapture() {
        Log.i(TAG, "Canceling Capture");
        mCapturing = false;
        mBiometricClient.cancel();
    }

    protected CompletionHandler<NBiometricStatus, NSubject> createCompletionHandler() {
        final FaceAuthActivity activity = this;
        return new CompletionHandler<NBiometricStatus, NSubject>() {
            @Override
            public void completed(NBiometricStatus status, NSubject subject) {
                if (status == NBiometricStatus.OK) {
                    Log.i(TAG, "Biometric Capture was successful");
                    activity.onCaptureSuccess(subject);
                } else {
                    Log.i(TAG, "Biometric Capture was not successful");
                    showMessage(R.string.bio_status_not_ok);
                    if (mCapturing) {
                        startCapture();
                    }
                }
            }

            @Override
            public void failed(Throwable e, NSubject subject) {
                Log.e(TAG, "Biometric Capture failed", e);
                activity.onCaptureFailure(e);
            }
        };
    }

    protected void setCaptureButtonEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaptureButton.setEnabled(enabled);
            }
        });
    }

    private NDevice connectCamera(NDeviceManager deviceManager) {
        NDeviceManager.DeviceCollection devices = deviceManager.getDevices();
        int count = devices.size();
        if (count == 0) {
            throw new RuntimeException("No cameras found, exiting!");
        }
        for (int i = 0; i < count; i++) {
            NDevice device = devices.get(i);
            if (device.getDisplayName().contains("Front")) {
                return device;
            }
        }
        return devices.get(0);
    }

    protected void showMessage(final int messageResource) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, getString(messageResource));
                Toast.makeText(context, messageResource, Toast.LENGTH_LONG).show();
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

    protected abstract void onCaptureSuccess(NSubject subject);

    protected abstract void onCaptureFailure(Throwable e);
}
