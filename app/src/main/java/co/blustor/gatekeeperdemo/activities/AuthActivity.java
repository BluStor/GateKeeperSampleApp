package co.blustor.gatekeeperdemo.activities;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.AuthFragment;

public class AuthActivity extends CardActivity {
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_auth;
    }

    @Override
    protected void setInitialFragment() {
        pushFragment(new AuthFragment(), AuthFragment.TAG);
    }
}
