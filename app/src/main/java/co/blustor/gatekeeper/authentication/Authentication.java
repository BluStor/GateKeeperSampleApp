package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient;

public interface Authentication {
    Status signInWithFace(NSubject testSubject) throws IOException;
    Status enrollWithFace(NSubject subject) throws IOException;
    boolean revokeFace() throws IOException;
    List<Object> listTemplates();

    enum Status {
        SUCCESS,
        CANCELED,
        UNAUTHORIZED,
        BAD_TEMPLATE;

        public static Status fromCardResponse(CardClient.Response response) {
            switch (response.getStatus()) {
                case 226:
                    return Status.SUCCESS;
                case 426:
                    return Status.CANCELED;
                case 430:
                    return Status.UNAUTHORIZED;
                case 501:
                    return Status.BAD_TEMPLATE;
                default:
                    throw new RuntimeException(response.getStatusMessage());
            }
        }
    }
}
