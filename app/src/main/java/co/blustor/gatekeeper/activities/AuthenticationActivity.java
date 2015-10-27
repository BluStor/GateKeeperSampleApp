package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.util.concurrent.CompletionHandler;

import java.io.IOException;
import java.util.EnumSet;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class AuthenticationActivity extends Activity {
    public String TAG = AuthenticationActivity.class.getSimpleName();

    private NFaceView mFaceView;
    private Button mCaptureButton;

    private Datastore mDatastore;
    private NBiometricClient mBiometricClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatastore = DroidDatastore.getInstance(this);
        setContentView(R.layout.activity_authentication);
        initializeViews();
        initializeClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCapturing();
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
                stopCapturing();
            }
        });
    }

    private void initializeClient() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.setUseDeviceManager(true);
        NDeviceManager deviceManager = mBiometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        mBiometricClient.initialize();
        startCapturing();
    }

    private void startCapturing() {
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        mFaceView.setFace(face);
        subject.getFaces().add(face);

        NCamera camera = (NCamera) connectCamera(mBiometricClient.getDeviceManager());
        mBiometricClient.setFaceCaptureDevice(camera);
        mBiometricClient.capture(subject, subject, completionHandler);
        showMessage(R.string.turn_camera_to_face);
    }

    private void stopCapturing() {
        mBiometricClient.force();
    }

    private void startResultsActivity(EnrollmentResultActivity.Result result) {
        Intent resultIntent = new Intent(AuthenticationActivity.this, EnrollmentResultActivity.class);
        resultIntent.putExtra(EnrollmentResultActivity.RESULT_KEY, result);
        startActivity(resultIntent);
        finish();
    }



    private CompletionHandler<NBiometricStatus, NSubject> completionHandler = new CompletionHandler<NBiometricStatus, NSubject>() {
        @Override
        public void completed(final NBiometricStatus result, NSubject capturedSubject) {
            if (result == NBiometricStatus.OK) {
                try {
                    NSubject storedSubject = mDatastore.getTemplate();

                    NBiometricTask enrollTask = mBiometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), storedSubject);
                    NBiometricTask identifyTask = mBiometricClient.createTask(EnumSet.of(NBiometricOperation.IDENTIFY), capturedSubject);

                    mBiometricClient.performTask(enrollTask, NBiometricOperation.ENROLL, new CompletionHandler<NBiometricTask, NBiometricOperation>() {
                        @Override
                        public void completed(NBiometricTask result, NBiometricOperation nBiometricOperation) {
                            Log.e(TAG, "Enrollment completed with status: " + result.getStatus());
                        }

                        @Override
                        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
                            Log.e(TAG, "Enrollment failed.");
                        }
                    });

                    mBiometricClient.performTask(identifyTask, NBiometricOperation.IDENTIFY, new CompletionHandler<NBiometricTask, NBiometricOperation>() {
                        @Override
                        public void completed(NBiometricTask result, NBiometricOperation nBiometricOperation) {
                            if (result.getStatus() == NBiometricStatus.OK) {
                                Log.e(TAG, "You are authenticated.");
                                startActivity(new Intent(AuthenticationActivity.this, FileBrowserActivity.class));
                            } else {
                                Log.e(TAG, "Authentication failed.  Result Status: " + result.getStatus());
                            }
                        }

                        @Override
                        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {

                        }
                    });



                } catch (IOException e) {
                    Log.e(TAG, "Failed to load template.  " + e.toString());
                }
            } else {
                showMessage(R.string.bio_status_not_ok);
                startResultsActivity(EnrollmentResultActivity.Result.SUBJECT_NOT_CAPTURED);
            }
        }

        @Override
        public void failed(Throwable exc, NSubject subject) {
            Log.e(TAG, "Biometric Capture Failed!");
            exc.printStackTrace();
            startResultsActivity(EnrollmentResultActivity.Result.CAPTURE_FAILED);
        }
    };

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

    private void showMessage(final int messageResource) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, getString(messageResource));
                Toast.makeText(context, messageResource, Toast.LENGTH_LONG).show();
            }
        });
    }
}
