package co.blustor.gatekeeper.data;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;

public interface Filestore {
    void storeTemplate(NSubject subject) throws IOException;
}
