package co.blustor.gatekeeperdemo.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import co.blustor.gatekeeperdemo.R;

public class RequestPairDialogFragment extends DialogFragment {
    public static final String TAG = RequestPairDialogFragment.class.getSimpleName();

    private static final int REQUEST_CARD_PAIR = 1;

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.gkcard_pair_requested_title))
               .setMessage(getString(R.string.gkcard_pair_requested_message))
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       requestPairWithCard();
                   }
               })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getActivity().finish();
                   }
               });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return builder.create();
    }

    private void requestPairWithCard() {
        Intent enableBtIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivityForResult(enableBtIntent, REQUEST_CARD_PAIR);
    }
};
