package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.List;

public interface Authentication {
    boolean signInWithFace(NSubject testSubject) throws IOException;
    boolean enrollWithFace(NSubject subject) throws IOException;
    boolean revokeFace() throws IOException;
    List<Object> listTemplates();
}
