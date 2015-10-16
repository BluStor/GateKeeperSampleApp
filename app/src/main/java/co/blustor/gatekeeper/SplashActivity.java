package co.blustor.gatekeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity implements Environment.InitializationListener {
    public final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Environment env = Environment.getInstance(this);
        env.initialize(this);
    }

    @Override
    public void onStatusChanged(Environment.Status status) {
    }

    @Override
    public void onComplete(Environment.Status status) {
        startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
        finish();
    }
}
