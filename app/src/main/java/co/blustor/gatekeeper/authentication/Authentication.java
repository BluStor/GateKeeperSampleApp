package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient;

public interface Authentication {
    AuthResult signInWithFace(NSubject testSubject) throws IOException;
    AuthResult enrollWithFace(NSubject subject) throws IOException;
    boolean revokeFace() throws IOException;
    List<Object> listTemplates();

    enum Status {
        SUCCESS,
        CANCELED,
        UNAUTHORIZED,
        BAD_TEMPLATE
    }

    class AuthResult {
        public final Status status;

        public AuthResult(Status status) {
            this.status = status;
        }

        public static AuthResult fromCardResponse(CardClient.Response response) {
            switch (response.getStatus()) {
                case 226:
                    return new AuthResult(Status.SUCCESS);
                case 426:
                    return new AuthResult(Status.CANCELED);
                case 430:
                    return new AuthResult(Status.UNAUTHORIZED);
                case 501:
                    return new AuthResult(Status.BAD_TEMPLATE);
                default:
                    throw new RuntimeException(response.getStatusMessage());
            }
        }
    }
}
