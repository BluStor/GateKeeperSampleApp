package co.blustor.gatekeeper.demo;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.doubles.AndroidCardDouble;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeperdemo.Application;

public class Configuration implements Application.Configuration {
    public static final String TAG = Configuration.class.getSimpleName();

    @Override
    public GKAuthentication getAuthentication() {
        return new GKAuthentication(getGKCard()) {
            @Override
            public List<Object> listTemplates() throws IOException {
                return new ArrayList<>();
            }

            @Override
            public Status revokeFace() throws IOException {
                return Status.SUCCESS;
            }

            @Override
            public Status signOut() throws IOException {
                return Status.SUCCESS;
            }

            @Override
            public Status enrollWithFace(NSubject subject) throws IOException {
                return Status.SUCCESS;
            }

            @Override
            public Status signInWithFace(NSubject subject) throws IOException {
                return Status.AUTHENTICATED;
            }
        };
    }

    @Override
    public GKCard getGKCard() {
        return new AndroidCardDouble();
    }
}
