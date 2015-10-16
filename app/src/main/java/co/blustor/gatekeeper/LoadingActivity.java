package co.blustor.gatekeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoadingActivity extends Activity implements Environment.InitializationListener {
    public final String TAG = LoadingActivity.class.getSimpleName();

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
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }
}
