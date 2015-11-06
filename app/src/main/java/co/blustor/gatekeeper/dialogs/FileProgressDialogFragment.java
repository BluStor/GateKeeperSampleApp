package co.blustor.gatekeeper.dialogs;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import co.blustor.gatekeeper.R;

public class FileProgressDialogFragment extends DialogFragment {
    int mText;

    public FileProgressDialogFragment() {
        mText = R.string.file_download_in_progress_text;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.file_progress, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.file_progress_text);
        textView.setText(mText);

        setCancelable(false);

        return builder.create();
    }

    public void setText(int text) {
        mText = text;
    }
}
