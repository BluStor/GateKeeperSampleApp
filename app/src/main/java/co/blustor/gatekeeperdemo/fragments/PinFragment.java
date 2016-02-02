package co.blustor.gatekeeperdemo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import co.blustor.gatekeeperdemo.R;

public class PinFragment extends Fragment {
    public static final String TAG = PinFragment.class.getSimpleName();
    private EditText mPinDigitsTextView;
    private Button mEnrollButton;
    private OnPinDigitsEnteredListener mDigitsEnteredListener;

    public interface OnPinDigitsEnteredListener {
        void onPinDigitsEntered(String digits);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mDigitsEnteredListener = (OnPinDigitsEnteredListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, context.getClass() + "does not implement OnPinDigitsEnteredListener", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pin_fragment, container);
        mPinDigitsTextView = (EditText) view.findViewById(R.id.pin_digits);
        mEnrollButton = (Button) view.findViewById(R.id.enroll_pin_button);
        mEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDigitsEnteredListener.onPinDigitsEntered(mPinDigitsTextView.getText().toString());
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
