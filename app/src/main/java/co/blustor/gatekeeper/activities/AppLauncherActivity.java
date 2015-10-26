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

public class AppLauncherActivity extends Activity {
    private Datastore mDatastore;
    private Button mLaunchFileBrowserButton;
    private Button mResetCardButton;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_launcher);

        mDatastore = DroidDatastore.getInstance(this);
        mLaunchFileBrowserButton = (Button) findViewById(R.id.launch_file_browser_button);
        mResetCardButton = (Button) findViewById(R.id.reset_card_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(AppLauncherActivity.this);
        builder.setMessage("Are you sure you want to reset the card?  " +
                "This will erase the stored face template and you will have to enroll again.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatastore.deleteTemplate();
                Toast toast = Toast.makeText(AppLauncherActivity.this, "Face template removed.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        builder.setNegativeButton("No", null);

        mAlertDialog = builder.create();

        mLaunchFileBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppLauncherActivity.this, FileBrowserActivity.class));
            }
        });

        mResetCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.show();
            }
        });
    }
}
