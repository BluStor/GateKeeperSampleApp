package co.blustor.gatekeeperdemo.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public class CardFragment extends Fragment {
    protected GKCard mCard;
    protected GKFaces mFaces;

    protected boolean mCardAvailable;

    public void setCard(GKCard card) {
        mCard = card;
    }

    public void setFaces(GKFaces faces) {
        mFaces = faces;
    }

    public void setCardAvailable(boolean available) {
        mCardAvailable = available;
    }

    public void updateUI() {
    }

    public void showPendingUI() {
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

    protected CardActivity getCardActivity() {
        return (CardActivity) getActivity();
    }

    public void onCardAccessUpdated() {
    }
}
