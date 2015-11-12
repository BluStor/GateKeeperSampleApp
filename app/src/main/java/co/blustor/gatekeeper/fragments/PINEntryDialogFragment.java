package co.blustor.gatekeeper.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import co.blustor.gatekeeper.R;

public class PINEntryDialogFragment extends DialogFragment {
    public final static String TAG = PINEntryDialogFragment.class.getSimpleName();

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_entry, container, false);
        final EditText pinInput = (EditText) view.findViewById(R.id.pin_input);
        pinInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return submitPin(pinInput);
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private boolean submitPin(EditText pinInput) {
        String pin = pinInput.getText().toString();
        if (pin.length() == 4) {
            if (mListener != null) {
                mListener.onSubmitPIN(pin);
            }
            getDialog().dismiss();
            return false;
        }
        return true;
    }

    public interface Listener {
        void onSubmitPIN(String pin);
    }
}
