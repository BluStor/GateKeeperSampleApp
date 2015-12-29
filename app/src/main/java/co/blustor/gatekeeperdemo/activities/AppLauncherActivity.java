package co.blustor.gatekeeperdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeper.authentication.GKCardAuthentication;
import co.blustor.gatekeeperdemo.Application;

public class AppLauncherActivity extends CardActivity {
    public static final String TAG = AppLauncherActivity.class.getSimpleName();

    private Button mOpenFileVaultButton;
    private Button mResetCardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_launcher);
        initializeButtons();
    }

    @Override
    public void onBackPressed() {
        promptSignOut();
    }

    private void initializeButtons() {
        mOpenFileVaultButton = (Button) findViewById(R.id.open_file_vault_button);
        mOpenFileVaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileVault();
            }
        });

        mResetCardButton = (Button) findViewById(R.id.reset_card_button);
        mResetCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDeleteTemplate();
            }
        });
    }

    private void openFileVault() {
        startActivity(new Intent(AppLauncherActivity.this, FileBrowserActivity.class));
    }

    private void promptDeleteTemplate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppLauncherActivity.this);
        builder.setMessage(R.string.reset_card_confirm);
        builder.setPositiveButton(R.string.delete_template_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int deleteMessage;
                try {
                    GKCardAuthentication authentication = Application.getAuthentication();
                    authentication.revokeFace();
                    deleteMessage = R.string.delete_template_success;
                } catch (IOException e) {
                    Log.e(TAG, "Unable to delete template", e);
                    deleteMessage = R.string.delete_template_failure;
                }
                Toast.makeText(AppLauncherActivity.this, deleteMessage, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.delete_template_no, null);
        builder.create().show();
    }
}
