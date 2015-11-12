package co.blustor.gatekeeper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.Datastore;
import co.blustor.gatekeeper.data.DroidDatastore;

public class AppLauncherActivity extends ActionBarActivity {
    private Datastore mDatastore;
    private Button mLaunchFileBrowserButton;
    private Button mResetCardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_launcher);

        mDatastore = DroidDatastore.getInstance(this);
        initializeButtons();
    }

    @Override
    public void onBackPressed() {
        promptSignOut(this);
    }

    private void initializeButtons() {
        mLaunchFileBrowserButton = (Button) findViewById(R.id.launch_file_browser_button);
        mLaunchFileBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppLauncherActivity.this, FileBrowserActivity.class));
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

    private void promptDeleteTemplate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppLauncherActivity.this);
        builder.setMessage(R.string.delete_template_confirm);
        builder.setPositiveButton(R.string.delete_template_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatastore.deleteTemplate();
                Toast.makeText(AppLauncherActivity.this, "Face template removed.", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.delete_template_no, null);
        builder.create().show();
    }

    private void promptSignOut(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppLauncherActivity.this);
        builder.setMessage(R.string.sign_out_confirm);
        builder.setPositiveButton(R.string.sign_out_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.setNegativeButton(R.string.sign_out_no, null);
        builder.create().show();
    }
}
