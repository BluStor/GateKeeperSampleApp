package co.blustor.gatekeeper.scopes;

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

public class GKAuthentication {
    public static final String TAG = GKAuthentication.class.getSimpleName();

    public static final String SIGN_IN_PATH = "/auth/signin";
    public static final String SIGN_OUT_PATH = "/auth/signout";
    public static final String ENROLL_FACE_PATH = "/auth/face000";
    public static final String REVOKE_FACE_PATH = "/auth/face000";
    public static final String LIST_FACE_PATH = "/auth";

    private final GKCard mGKCard;

    public GKAuthentication(GKCard gkCard) {
        mGKCard = gkCard;
    }

    public Status signInWithFace(NSubject subject) throws IOException {
        Response response = submitTemplate(subject, SIGN_IN_PATH);
        return Status.fromCardResponse(response);
    }

    public Status enrollWithFace(NSubject subject) throws IOException {
        Response response = submitTemplate(subject, ENROLL_FACE_PATH);
        return Status.fromCardResponse(response);
    }

    public Status signOut() throws IOException {
        Response response = mGKCard.delete(SIGN_OUT_PATH);
        return Status.fromCardResponse(response);
    }

    public Status revokeFace() throws IOException {
        Response response = mGKCard.delete(REVOKE_FACE_PATH);
        return Status.fromCardResponse(response);
    }

    public List<Object> listTemplates() throws IOException {
        Response response = mGKCard.list(LIST_FACE_PATH);
        String responseData = new String(response.getData());

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseData);

        List<Object> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    private Response submitTemplate(NSubject subject, String cardPath) throws IOException {
        NTemplate template = null;
        try {
            mGKCard.connect();
            template = subject.getTemplate();
            ByteArrayInputStream inputStream = getTemplateInputStream(template);
            Response response = mGKCard.put(cardPath, inputStream);
            if (response.getStatus() != 226) {
                return response;
            }
            return mGKCard.finalize(cardPath);
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

    public enum Status {
        SUCCESS,
        AUTHENTICATED,
        CANCELED,
        UNAUTHENTICATED,
        UNAUTHORIZED,
        BAD_TEMPLATE,
        NOT_FOUND,
        UNKNOWN_STATUS;

        public static Status fromCardResponse(Response response) {
            switch (response.getStatus()) {
                case 213:
                    return Status.SUCCESS;
                case 226:
                    return Status.SUCCESS;
                case 230:
                    return Status.AUTHENTICATED;
                case 231:
                    return Status.SUCCESS;
                case 250:
                    return Status.SUCCESS;
                case 426:
                    return Status.CANCELED;
                case 430:
                    return Status.UNAUTHENTICATED;
                case 501:
                    return Status.BAD_TEMPLATE;
                case 530:
                    return Status.UNAUTHORIZED;
                case 550:
                    return Status.NOT_FOUND;
                default:
                    return Status.UNKNOWN_STATUS;
            }
        }
    }
}
