package co.blustor.gatekeeper.authentication;


import com.neurotec.biometrics.NSubject;

public interface FaceAuthenticator {
    public boolean authenticate(NSubject testSubject);
}
