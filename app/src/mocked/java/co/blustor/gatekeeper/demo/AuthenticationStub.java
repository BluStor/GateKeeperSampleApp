package co.blustor.gatekeeper.demo;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.authentication.Authentication;

public class AuthenticationStub implements Authentication {
    public static final String TAG = AuthenticationStub.class.getSimpleName();

    @Override
    public boolean signInWithFace(NSubject testSubject) {
        return true;
    }

    @Override
    public boolean enrollWithFace(NSubject subject) {
        return true;
    }

    @Override
    public boolean revokeFace() throws IOException {
        return true;
    }

    @Override
    public List<Object> listTemplates() {
        return new ArrayList<>();
    }
}
