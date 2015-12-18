package co.blustor.gatekeeper.authentication;

import android.support.annotation.NonNull;

import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.devices.GKCard;

public class GKCardAuthentication implements Authentication {
    private final GKCard mGKCard;

    public GKCardAuthentication(GKCard gkCard) {
        mGKCard = gkCard;
    }

    @Override
    public boolean signInWithFace(NSubject subject) throws IOException {
        mGKCard.connect();
        NTemplate template = null;
        try {
            template = subject.getTemplate();
            ByteArrayInputStream inputStream = getTemplateInputStream(template);
            mGKCard.uploadFile("/auth/signin/face", inputStream);
        } finally {
            if (template != null) {
                template.dispose();
            }
        }
        return true;
    }

    @Override
    public boolean enrollWithFace(NSubject subject) throws IOException {
        mGKCard.connect();
        NTemplate template = null;
        try {
            template = subject.getTemplate();
            ByteArrayInputStream inputStream = getTemplateInputStream(template);
            mGKCard.uploadFile("/auth/face/0", inputStream);
        } finally {
            if (template != null) {
                template.dispose();
            }
        }
        return true;
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

    @NonNull
    private ByteArrayInputStream getTemplateInputStream(NTemplate template) {NLRecord faceRecord = template.getFaces().getRecords().get(0);
        byte[] buffer = faceRecord.save().toByteArray();
        return new ByteArrayInputStream(buffer);
    }
}
