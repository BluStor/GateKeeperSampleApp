package co.blustor.gatekeeper.authentication;

import android.support.annotation.NonNull;

import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.devices.GKCard;

public class GKCardAuthentication implements Authentication {
    public final static String TAG = GKCardAuthentication.class.getSimpleName();

    private final GKCard mGKCard;

    public GKCardAuthentication(GKCard gkCard) {
        mGKCard = gkCard;
    }

    @Override
    public Status signInWithFace(NSubject subject) throws IOException {
        return submitTemplate(subject, "/auth/signin/face");
    }

    @Override
    public Status enrollWithFace(NSubject subject) throws IOException {
        return submitTemplate(subject, "/auth/face/0");
    }

    @Override
    public boolean revokeFace() throws IOException {
        return false;
    }

    @Override
    public List<Object> listTemplates() {
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(null);
        return objects;
    }

    private Status submitTemplate(NSubject subject, String cardPath) throws IOException {
        NTemplate template = null;
        try {
            mGKCard.connect();
            template = subject.getTemplate();
            ByteArrayInputStream inputStream = getTemplateInputStream(template);
            CardClient.Response response = mGKCard.store(cardPath, inputStream);
            return Status.fromCardResponse(response);
        } finally {
            if (template != null) {
                template.dispose();
            }
        }
    }

    @NonNull
    private ByteArrayInputStream getTemplateInputStream(NTemplate template) {
        NLRecord faceRecord = template.getFaces().getRecords().get(0);
        byte[] buffer = faceRecord.save().toByteArray();
        return new ByteArrayInputStream(buffer);
    }
}
