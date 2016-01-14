package co.blustor.gatekeeperdemo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public abstract class CardFragment extends Fragment {
    protected GKCard mCard;
    protected GKFaces mFaces;

    protected boolean mCardAvailable;

    protected GKCard.Monitor mCardMonitor;

    public void setCard(GKCard card) {
        mCard = card;
    }

    public void setFaces(GKFaces faces) {
        mFaces = faces;
    }

    public void updateUI() {
    }

    public void showPendingUI() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCardMonitor = new UICardMonitor() {
            @Override
            protected void updateConnectionStateUI(GKCard.ConnectionState state) {
                if (state.equals(GKCard.ConnectionState.CONNECTED)) {
                    setCardAvailable(true);
                } else {
                    setCardAvailable(false);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectionStateUI(mCard.getConnectionState());
        mCard.addMonitor(mCardMonitor);
    }

    @Override
    public void onPause() {
        mCard.removeMonitor(mCardMonitor);
        super.onPause();
    }

    public boolean canNavigateBack() {
        return false;
    }

    public void navigateBack() {
    }

    public void showMessage(int messageResource) {
        Log.i(getTag(), getString(messageResource));
        Toast toast = Toast.makeText(getContext(), messageResource, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void setCardAvailable(boolean available) {
        mCardAvailable = available;
    }

    protected void updateConnectionStateUI(GKCard.ConnectionState state) {
        if (state.equals(GKCard.ConnectionState.CONNECTED)) {
            setCardAvailable(true);
        } else {
            setCardAvailable(false);
        }
    }

    protected CardActivity getCardActivity() {
        return (CardActivity) getActivity();
    }

    public void onCardAccessUpdated() {
    }

    private abstract class UICardMonitor implements GKCard.Monitor {
        protected abstract void updateConnectionStateUI(GKCard.ConnectionState state);

        @Override
        public void onStateChanged(final GKCard.ConnectionState state) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectionStateUI(state);
                }
            });
        }
    }
}
