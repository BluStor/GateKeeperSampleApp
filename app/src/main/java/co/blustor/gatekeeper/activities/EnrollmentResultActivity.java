package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import co.blustor.gatekeeper.R;

public class EnrollmentResultActivity extends Activity {
    public static final String RESULT_KEY = "authResult";

    public enum Result {
        SUCCESS,
        TEMPLATE_NOT_STORED,
        SUBJECT_NOT_CAPTURED,
        CAPTURE_FAILED;
    }

    private TextView mResultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_result);
        mResultMessage = (TextView) findViewById(R.id.result_message);
        setResultMessage();
    }

    private void setResultMessage() {
        Result result = (Result) getIntent().getExtras().get(RESULT_KEY);
        switch (result) {
            case SUCCESS:
                mResultMessage.setText(R.string.enrollment_result_success);
                break;
            case TEMPLATE_NOT_STORED:
                mResultMessage.setText(R.string.template_not_stored);
                break;
            case SUBJECT_NOT_CAPTURED:
                mResultMessage.setText(R.string.subject_not_captured);
                break;
            default:
                mResultMessage.setText(R.string.capture_failed);
        }
    }
}
