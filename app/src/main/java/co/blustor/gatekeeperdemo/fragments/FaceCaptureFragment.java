package co.blustor.gatekeeperdemo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;

public class FaceCaptureFragment extends CardFragment {
    public static final String TAG = FaceCaptureFragment.class.getSimpleName();
    private Button mCaptureButton;
    private LinearLayout mCameraContainer;
    private OnTemplateCapturedListener mTemplateCapturedListener;

    public interface OnTemplateCapturedListener {
        void onTemplateCaptured(final GKFaces.Template template);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mTemplateCapturedListener = (OnTemplateCapturedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e + " " + "Class cannot be cast to OnTemplateCapturedListener");
        }
    }

    @Override
    public void onStop() {
        mFaces.finishCameraCapture();
        super.onStop();
    }

    @Override
    public void onResume() {
        setFaces(Application.getGKFaces());
        mFaces.setupCamera(getCardActivity(), mCameraContainer, mOnCameraCompletionListener);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.face_capture_fragment, container);
        mCameraContainer = (LinearLayout) view.findViewById(R.id.face_capture);

        mCaptureButton = (Button) view.findViewById(R.id.face_capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureButton.setEnabled(false);
                mFaces.captureImage();
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    GKFaces.OnCameraCompletionListener mOnCameraCompletionListener = new GKFaces.OnCameraCompletionListener() {
        @Override
        public Void onSuccess(GKFaces.Template template) {
            mTemplateCapturedListener.onTemplateCaptured(template);
            return null;
        }

        @Override
        public Void onFailure() {
            return null;
        }
    };
}
