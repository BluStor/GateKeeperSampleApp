package co.blustor.gatekeeper.data;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

public class GKCardSettings {
    private final GKCard mCard;

    public GKCardSettings(GKCard card) {
        mCard = card;
    }

    public Response updateFirmware(InputStream inputStream) throws IOException {
        mCard.connect();
        return mCard.store("/device/firmware", inputStream);
    }
}
