package co.blustor.gatekeeper.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import co.blustor.gatekeeper.R;

public abstract class ActionBarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setIcon(R.drawable.ic_blustor_logo_android_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showSettingsActivity();
                return true;
            case R.id.sign_out:
                promptSignOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void promptSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sign_out_confirm);
        builder.setPositiveButton(R.string.sign_out_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSignOut();
            }
        });
        builder.setNegativeButton(R.string.sign_out_no, null);
        builder.create().show();
    }

    protected void onSignOut() {
        this.finishAffinity();
    }
}
