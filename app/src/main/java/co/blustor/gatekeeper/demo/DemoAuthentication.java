package co.blustor.gatekeeper.demo;

import com.neurotec.biometrics.NSubject;

import co.blustor.gatekeeper.authentication.Authentication;

public class DemoAuthentication implements Authentication {
    public static final String TAG = DemoAuthentication.class.getSimpleName();

    @Override
    public boolean signInWithFace(NSubject testSubject) {
        return true;
    }
}
