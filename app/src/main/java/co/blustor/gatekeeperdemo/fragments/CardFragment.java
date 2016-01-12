package co.blustor.gatekeeperdemo.fragments;

import android.support.v4.app.Fragment;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.activities.CardActivity;

public class CardFragment extends Fragment {
    protected GKCard mCard;

    public void setCard(GKCard card) {
        mCard = card;
    }

    public void setCardAvailable(boolean available) {
    }

    public boolean canNavigateBack() {
        return false;
    }

    public void navigateBack() {
    }

    protected CardActivity getCardActivity() {
        return (CardActivity) getActivity();
    }
}
