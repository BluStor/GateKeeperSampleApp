package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import co.blustor.gatekeeperdemo.R;

public class FileProgressDialogFragment extends DialogFragment {
    public static final String TAG = FileProgressDialogFragment.class.getSimpleName();

    int mText;

    public FileProgressDialogFragment() {
        mText = R.string.file_get_progress_message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_file_progress, null);

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
