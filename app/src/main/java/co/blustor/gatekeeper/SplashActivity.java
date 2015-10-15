package co.blustor.gatekeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {
    public final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(startNextActivity);
        thread.start();
    }

    private Runnable startNextActivity = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
                finish();
            } catch (Exception e) {
                Log.e(TAG, "something went wrong", e);
                finish();
            }
        }
    };
}
