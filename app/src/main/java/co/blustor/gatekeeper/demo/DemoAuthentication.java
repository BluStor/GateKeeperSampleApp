package co.blustor.gatekeeper.demo;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.authentication.Authentication;

public class DemoAuthentication implements Authentication {
    public static final String TAG = DemoAuthentication.class.getSimpleName();

    @Override
    public Status signInWithFace(NSubject subject) {
        return Status.SUCCESS;
    }

    @Override
    public Status enrollWithFace(NSubject subject) {
        return Status.SUCCESS;
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
