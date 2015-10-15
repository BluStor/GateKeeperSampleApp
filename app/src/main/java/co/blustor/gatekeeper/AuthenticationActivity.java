package co.blustor.gatekeeper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.neurotec.biometrics.NBiometric;
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
import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicense;
import com.neurotec.util.concurrent.CompletionHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.EnumSet;

public class AuthenticationActivity extends Activity {
    public String TAG = AuthenticationActivity.class.getSimpleName();

    public static final String[] LICENSES = {
            "Biometrics.FaceExtraction",
            "Biometrics.FaceDetection",
            "Devices.Cameras"
    };

    private NFaceView mFaceView;
    private Button mCaptureButton;

    private NBiometricClient mBiometricClient;
    private final String sHostAddress = "192.168.0.20";
    private final int sHostPort = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NCore.setContext(this);
        setContentView(R.layout.activity_authentication);
        obtainLicenses();
        initializeViews();
        initializeClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLicenses();
        stopCapturing();
        if (mBiometricClient != null) {
            mBiometricClient.cancel();
            mBiometricClient.dispose();
            mBiometricClient = null;
        }
    }

    private void obtainLicenses() {
        for (String component : LICENSES) {
            try {
                NLicense.obtainComponents(sHostAddress, sHostPort, component);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("licenses were not obtained");
            }
        }
    }

    private void releaseLicenses() {
        for (String component : LICENSES) {
            try {
                NLicense.releaseComponents(component);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("licenses were not released");
            }
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
        face.addPropertyChangeListener(biometricPropertyChanged);
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

    private final PropertyChangeListener biometricPropertyChanged = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("Status".equals(evt.getPropertyName())) {
                final NBiometricStatus status = ((NBiometric) evt.getSource()).getStatus();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (status == NBiometricStatus.OK) {
                            showMessage(R.string.bio_status_ok);
                        } else {
                            showMessage(R.string.bio_status_not_ok);
                        }
                        Log.i(TAG, "Biometric Face Status: " + status.toString());
                    }
                });
            }
        }
    };

    private CompletionHandler<NBiometricStatus, NSubject> completionHandler = new CompletionHandler<NBiometricStatus, NSubject>() {
        @Override
        public void completed(NBiometricStatus result, NSubject subject) {
            if (result == NBiometricStatus.OK) {
                showMessage(R.string.bio_status_ok);
            } else {
                showMessage(R.string.bio_status_not_ok);
            }
            Log.i(TAG, "Biometric Capture Result: " + result.toString());
        }

        @Override
        public void failed(Throwable exc, NSubject subject) {
            Log.e(TAG, "Biometric Capture Failed!");
            exc.printStackTrace();
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
                Toast.makeText(context, messageResource, Toast.LENGTH_LONG);
            }
        });
    }
}
