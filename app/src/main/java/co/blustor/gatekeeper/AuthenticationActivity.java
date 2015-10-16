package co.blustor.gatekeeper;

import android.app.Activity;
import android.os.Bundle;

public class AuthenticationActivity extends Activity {
    public String TAG = AuthenticationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
    }
}
