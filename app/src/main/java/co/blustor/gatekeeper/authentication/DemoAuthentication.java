package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

public class DemoAuthentication implements Authentication {
    public static final String TAG = DemoAuthentication.class.getSimpleName();

    @Override
    public boolean signInWithFace(NSubject testSubject) {
        return true;
    }
}
