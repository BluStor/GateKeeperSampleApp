package co.blustor.gatekeeperdemo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.fragments.SettingsFragment;
import co.blustor.gatekeeperdemo.fragments.TestsFragment;

public abstract class CardActivity extends BaseActivity {
    protected GKCard mCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCard = Application.getGKCard();
        try {
            mCard.connect();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        CardFragment currentFragment = getCurrentFragment();
        if (canHandleBackPressed(currentFragment)) {
            currentFragment.navigateBack();
        } else if (isAtRootFragment() && isTaskRoot()) {
            promptSignOut();
        } else {
            super.onBackPressed();
        }
    }

    public void showRetryConnectDialog() {
        mRetryConnectDialog.show(getSupportFragmentManager(), "retryConnectToCard");
    }

    protected void openSettings() {
        pushFragment(new SettingsFragment(), SettingsFragment.TAG);
    }

    protected void openTests() {
        pushFragment(new TestsFragment(), TestsFragment.TAG);
    }

    protected void promptSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sign_out_confirm);
        builder.setPositiveButton(R.string.sign_out_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSignOut();
            }
        });
        builder.setNegativeButton(R.string.sign_out_no, null);
        builder.create().show();
    }

    protected void onSignOut() {
        final Activity activity = this;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCard.connect();
                    GKAuthentication auth = new GKAuthentication(mCard);
                    auth.signOut();
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.finishAffinity();
            }
        }.execute();
    }

    @Override
    protected void pushFragment(CardFragment fragment, String tag) {
        fragment.setCard(mCard);
        super.pushFragment(fragment, tag);
    }

    protected void connectToCard() {
        getCurrentFragment().setCardAvailable(false);
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
                    getCurrentFragment().setCardAvailable(true);
                }
            }
        }.execute();
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
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
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
