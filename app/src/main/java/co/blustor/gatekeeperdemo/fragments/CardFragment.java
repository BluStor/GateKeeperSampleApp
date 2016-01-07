package co.blustor.gatekeeperdemo.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.R;

public class CardFragment extends Fragment {
    protected GKCard mCard;

    public void setCard(GKCard card) {
        mCard = card;
    }

    protected void connectToCard() {
        setCardAvailable(false);
        new AsyncTask<Void, Void, Void>() {
            private IOException ioException;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
                } catch (IOException e) {
                    ioException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (ioException != null) {
                    showRetryConnectDialog();
                } else {
                    setCardAvailable(true);
                }
            }
        }.execute();
    }

    public boolean canNavigateBack() {
        return false;
    }

    public void navigateBack() {
    }

    protected void setCardAvailable(boolean available) {
    }

    protected void showRetryConnectDialog() {
        mRetryConnectDialog.show(getFragmentManager(), "retryConnectToCard");
    }

    private DialogFragment mRetryConnectDialog = new DialogFragment() {
        public final String TAG = DialogFragment.class.getSimpleName();

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
            builder.setTitle(getString(R.string.gkcard_reconnect_prompt_title))
                   .setMessage(getString(R.string.gkcard_reconnect_prompt_message))
                   .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           connectToCard();
                       }
                   })
                   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           getActivity().finishAffinity();
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
    };
}
