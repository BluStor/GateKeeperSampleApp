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

    protected GKCard.Monitor mCardMonitor;
    protected GKCard.ConnectionState mCardState = GKCard.ConnectionState.DISCONNECTED;

    public void setCard(GKCard card) {
        mCard = card;
        mCardState = mCard.getConnectionState();
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

        mCardMonitor = new UICardMonitor();
        Log.d("WATWATWAT", "CREATING CardFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        onCardStateChanged(mCard.getConnectionState());
        mCard.addMonitor(mCardMonitor);
        Log.d("WATWATWAT", "RESUMING CardFragment");
    }

    @Override
    public void onPause() {
        mCard.removeMonitor(mCardMonitor);
        Log.d("WATWATWAT", "PAUSING CardFragment");
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

    protected boolean cardIsAvailable() {
        return mCardState.equals(GKCard.ConnectionState.CONNECTED);
    }

    protected void onCardStateChanged(GKCard.ConnectionState state) {
        mCardState = state;
    }

    protected CardActivity getCardActivity() {
        return (CardActivity) getActivity();
    }

    public void onCardAccessUpdated() {
    }

    private class UICardMonitor implements GKCard.Monitor {
        @Override
        public void onStateChanged(final GKCard.ConnectionState state) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onCardStateChanged(state);
                }
            });
            Log.d("WATWATWAT", "STATE CHANGED in UICardMonitor (CardFragment) to:" + state);
        }
    }
}
