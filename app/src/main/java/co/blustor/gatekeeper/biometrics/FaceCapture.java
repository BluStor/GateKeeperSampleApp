package co.blustor.gatekeeper.biometrics;

import android.support.annotation.NonNull;
import android.util.Log;

import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.util.concurrent.CompletionHandler;

import java.util.EnumSet;

public class FaceCapture {
    public static final String TAG = FaceCapture.class.getSimpleName();

    private static final FaceCapture sInstance = new FaceCapture();

    private NBiometricClient mBiometricClient;
    private NFace mFace;

    private boolean mStarted = false;

    @NonNull
    private Listener mListener;

    private FaceCapture() {
        mListener = mNullListener;
    }

    public static FaceCapture getInstance() {
        return sInstance;
    }

    public NFace getFace() {
        return mFace;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = mNullListener;
    }

    public void start() {
        if (!mStarted) {
            initializeCameraClient();
            NSubject subject = createCaptureSubject();

            Log.i(TAG, "Starting Capture");
            mBiometricClient.capture(subject, subject, createCompletionHandler());
            mStarted = true;
        }
    }

    public void complete() {
        Log.i(TAG, "Completing Capture");
        if (mBiometricClient != null) {
            mBiometricClient.force();
        }
        mStarted = false;
    }

    public void stop() {
        Log.i(TAG, "Stopping Capture");
        mStarted = false;
        if (mBiometricClient != null) {
            mBiometricClient.cancel();
            mBiometricClient.dispose();
            mBiometricClient = null;
        }
    }

    private CompletionHandler<NBiometricStatus, NSubject> createCompletionHandler() {
        return new CompletionHandler<NBiometricStatus, NSubject>() {
            @Override
            public void completed(NBiometricStatus status, NSubject subject) {
                if (status == NBiometricStatus.OK) {
                    Log.i(TAG, "Biometric Capture was successful");
                    onCaptureComplete(subject);
                } else {
                    Log.i(TAG, "Biometric Capture was not successful");
                    onCaptureIncomplete();
                }
            }

            @Override
            public void failed(Throwable e, NSubject subject) {
                Log.e(TAG, "Biometric Capture failed", e);
                onCaptureFailure();
            }
        };
    }

    private void initializeCameraClient() {
        Log.i(TAG, "Initializing Biometrics");
        mBiometricClient = new NBiometricClient();
        mBiometricClient.setUseDeviceManager(true);
        NDeviceManager deviceManager = mBiometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        mBiometricClient.initialize();
        connectCamera();
    }

    private void connectCamera() {
        NDeviceManager deviceManager = mBiometricClient.getDeviceManager();
        NDeviceManager.DeviceCollection devices = deviceManager.getDevices();
        int count = devices.size();
        if (count == 0) {
            throw new RuntimeException("No cameras found, exiting!");
        }

        NDevice selectedDevice = devices.get(0);
        for (int i = 0; i < count; i++) {
            NDevice device = devices.get(i);
            if (device.getDisplayName().contains("Front")) {
                selectedDevice = device;
            }
        }

        mBiometricClient.setFaceCaptureDevice((NCamera) selectedDevice);
    }

    @NonNull
    private NSubject createCaptureSubject() {
        mFace = new NFace();
        mFace.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        NSubject subject = new NSubject();
        subject.getFaces().add(mFace);
        return subject;
    }

    private void onCaptureIncomplete() {
        mListener.onCaptureIncomplete();
    }

    private void onCaptureComplete(NSubject subject) {
        mListener.onCaptureComplete(subject);
    }

    private void onCaptureFailure() {
        mListener.onCaptureFailure();
    }

    private Listener mNullListener = new Listener() {
        @Override
        public void onCaptureIncomplete() {
        }

        @Override
        public void onCaptureComplete(NSubject subject) {
        }

        @Override
        public void onCaptureFailure() {
        }
    };

    public interface Listener {
        void onCaptureIncomplete();
        void onCaptureComplete(NSubject subject);
        void onCaptureFailure();
    }
}