package co.blustor.gatekeeper.authentication;

import android.support.annotation.NonNull;

import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

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
    public Status revokeFace() throws IOException {
        Response response = mGKCard.delete("/auth/face/0");
        return Status.fromCardResponse(response);
    }

    @Override
    public List<Object> listTemplates() throws IOException {
        Response response = mGKCard.retrieve("/auth/face");
        String responseData = new String(response.getData());

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseData);

        List<Object> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    private Status submitTemplate(NSubject subject, String cardPath) throws IOException {
        NTemplate template = null;
        try {
            mGKCard.connect();
            template = subject.getTemplate();
            ByteArrayInputStream inputStream = getTemplateInputStream(template);
            Response response = mGKCard.store(cardPath, inputStream);
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
