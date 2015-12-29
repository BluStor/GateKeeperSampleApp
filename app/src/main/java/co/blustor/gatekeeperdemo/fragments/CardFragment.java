package co.blustor.gatekeeperdemo.fragments;

import android.support.v4.app.Fragment;

import co.blustor.gatekeeper.devices.GKCard;

public class CardFragment extends Fragment {
    protected GKCard mCard;

    public void setCard(GKCard card) {
        mCard = card;
    }

    public boolean canNavigateBack() {
        return false;
    }

    public void navigateBack() {
    }
}
