package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.biometrics.Environment;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class LoadingActivity extends Activity implements Environment.InitializationListener {
    public static final String TAG = LoadingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Environment env = Environment.getInstance(this);
        env.initialize(this);
    }

    @Override
    public void onStatusChanged(Environment.Status status) {
    }

    @Override
    public void onComplete(Environment.Status status) {
        Datastore datastore = DroidDatastore.getInstance(this);
        if (datastore.hasTemplate()) {
            startActivity(new Intent(this, AuthenticationActivity.class));
        } else {
            startActivity(new Intent(this, EnrollmentActivity.class));
        }
        finish();
    }
}
