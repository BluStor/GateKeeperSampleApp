package co.blustor.gatekeeper.data;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;

public interface Datastore {
    void storeTemplate(NSubject subject) throws IOException;
    void deleteTemplate();
    NSubject getTemplate() throws IOException;
}
