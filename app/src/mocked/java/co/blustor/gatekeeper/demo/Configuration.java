package co.blustor.gatekeeper.demo;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;
import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.doubles.AndroidCardDouble;

public class Configuration implements Application.Configuration {
    public static final String TAG = Configuration.class.getSimpleName();

    @Override
    public GKAuthentication getAuthentication() {
        return new GKAuthentication(getGKCard()) {
            @Override
            public Status revokeFace() throws IOException {
                return Status.SUCCESS;
            }

            @Override
            public Status signOut() throws IOException {
                return Status.SIGNED_OUT;
            }

            @Override
            public Status enrollWithFace(NSubject subject) throws IOException {
                return Status.SUCCESS;
            }

            @Override
            public Status signInWithFace(NSubject subject) throws IOException {
                return Status.SIGNED_IN;
            }
        };
    }

    @Override
    public GKCard getGKCard() {
        return new AndroidCardDouble();
    }
}
