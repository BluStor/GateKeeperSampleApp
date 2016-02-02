package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.PinFragment;

public class PinActivity extends BaseActivity implements PinFragment.OnPinDigitsEnteredListener {
    public static final String PIN_NUMBER = "pinNumber";

    public static Intent createIntent(Context context) {
        return new Intent(context, PinActivity.class);
    }

    @Override
    protected void setInitialFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, new PinFragment(), PinFragment.TAG);
        if (hasFragment()) {
            transaction.addToBackStack(PinFragment.TAG);
        }
        transaction.commit();
    }

    @Override
    public void onPinDigitsEntered(String digits) {
        Intent intent = new Intent();
        intent.putExtra(PIN_NUMBER, digits);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
