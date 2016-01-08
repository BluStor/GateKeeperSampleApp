package co.blustor.gatekeeper.scopes;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

public class GKCardSettings {
    private static final String UPDATE_FIRMWARE_PATH = "/device/firmware";

    private final GKCard mCard;

    public enum Status {
        SUCCESS,
        UNAUTHORIZED,
        INVALID_DATA,
        UNKNOWN_STATUS
    }

    public GKCardSettings(GKCard card) {
        mCard = card;
    }

    public CardResult updateFirmware(InputStream inputStream) throws IOException {
        mCard.connect();
        Response response = mCard.put(UPDATE_FIRMWARE_PATH, inputStream);
        if (response.getStatus() != 226) {
            return new CardResult(response);
        }
        Response finalize = mCard.finalize(UPDATE_FIRMWARE_PATH);
        return new CardResult(finalize);
    }

    public class CardResult {
        protected final Response mResponse;
        protected final Status mStatus;

        public CardResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(response);
        }

        public Status getStatus() {
            return mStatus;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 213:
                return Status.SUCCESS;
            case 501:
                return Status.INVALID_DATA;
            case 530:
                return Status.UNAUTHORIZED;
            default:
                return Status.UNKNOWN_STATUS;
        }
    }
}
