package co.blustor.gatekeeperdemo.activities;

import co.blustor.gatekeeperdemo.fragments.DemoSetupFragment;

public class DemoSetupActivity extends CardActivity {
    @Override
    protected void setInitialFragment() {
        pushFragment(new DemoSetupFragment(), DemoSetupFragment.TAG);
    }
}
