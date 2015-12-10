package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

public interface Authentication {
    boolean signInWithFace(NSubject testSubject);
}
