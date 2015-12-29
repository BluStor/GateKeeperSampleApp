package co.blustor.gatekeeperdemo.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public class AppLauncherFragment extends CardFragment {
    public static final String TAG = AppLauncherFragment.class.getSimpleName();

    private Button mOpenFileVaultButton;
    private Button mResetCardButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_launcher, container, false);
        mOpenFileVaultButton = (Button) view.findViewById(R.id.open_file_vault_button);
        mOpenFileVaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CardActivity) getActivity()).openFileVault();
            }
        });
        mResetCardButton = (Button) view.findViewById(R.id.reset_card_button);
        mResetCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDeleteTemplate();
            }
        });
        return view;
    }

    private void promptDeleteTemplate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.reset_card_confirm);
        builder.setPositiveButton(R.string.delete_template_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int deleteMessage;
                try {
                    GKAuthentication authentication = Application.getAuthentication();
                    authentication.revokeFace();
                    deleteMessage = R.string.delete_template_success;
                } catch (IOException e) {
                    Log.e(TAG, "Unable to delete template", e);
                    deleteMessage = R.string.delete_template_failure;
                }
                Toast.makeText(getContext(), deleteMessage, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.delete_template_no, null);
        builder.create().show();
    }
}
