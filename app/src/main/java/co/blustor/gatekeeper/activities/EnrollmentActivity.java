package co.blustor.gatekeeper.activities;

import android.os.Bundle;

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
            startResultsActivity(EnrollmentResultActivity.Result.SUCCESS);
        } catch (IOException e) {
            startResultsActivity(EnrollmentResultActivity.Result.TEMPLATE_NOT_STORED);
        }
    }

    @Override
    protected void onCaptureFailure(Throwable e) {
        startResultsActivity(EnrollmentResultActivity.Result.CAPTURE_FAILED);
    }
}
