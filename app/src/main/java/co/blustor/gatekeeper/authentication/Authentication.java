package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient;

public interface Authentication {
    boolean signInWithFace(NSubject testSubject) throws IOException;
    AuthResult enrollWithFace(NSubject subject);
    boolean revokeFace() throws IOException;
    List<Object> listTemplates();

    enum Status {
        SUCCESS,
        CANCELED,
        IO_ERROR,
        BAD_TEMPLATE,
        UNKNOWN_ERROR
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
                case 450:
                    return new AuthResult(Status.IO_ERROR);
                case 501:
                    return new AuthResult(Status.BAD_TEMPLATE);
                default:
                    return new AuthResult(Status.UNKNOWN_ERROR);
            }
        }
    }
}
