package co.blustor.gatekeeperdemo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public class AppLauncherFragment extends CardFragment {
    public static final String TAG = AppLauncherFragment.class.getSimpleName();

    private Button mOpenFileVaultButton;

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
        return view;
    }
}
