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
    public static final String ENROLL_FACE_PATH_PREFIX = "/auth/face00";
    public static final String REVOKE_FACE_PATH_PREFIX = "/auth/face00";
    public static final String LIST_FACE_PATH = "/auth";

    public enum Status {
        SUCCESS,
        SIGNED_IN,
        SIGNED_OUT,
        SIGN_IN_FAILURE,
        UNAUTHORIZED,
        BAD_TEMPLATE,
        NOT_FOUND,
        CANCELED,
        UNKNOWN_STATUS
    }

    protected final GKCard mGKCard;

    public GKAuthentication(GKCard gkCard) {
        mGKCard = gkCard;
    }

    public AuthResult enrollWithFace(NSubject subject) throws IOException {
        return enrollWithFace(subject, 0);
    }

    public AuthResult enrollWithFace(NSubject subject, int templateId) throws IOException {
        Response response = submitTemplate(subject, ENROLL_FACE_PATH_PREFIX + templateId);
        return new AuthResult(response);
    }

    public AuthResult signInWithFace(NSubject subject) throws IOException {
        Response response = submitTemplate(subject, SIGN_IN_PATH);
        return new AuthResult(response);
    }

    public AuthResult signOut() throws IOException {
        Response response = mGKCard.delete(SIGN_OUT_PATH);
        return new AuthResult(response);
    }

    public AuthResult revokeFace() throws IOException {
        return revokeFace(0);
    }

    public AuthResult revokeFace(int templateId) throws IOException {
        Response response = mGKCard.delete(REVOKE_FACE_PATH_PREFIX + templateId);
        return new AuthResult(response);
    }

    public ListTemplatesResult listTemplates() throws IOException {
        Response response = mGKCard.list(LIST_FACE_PATH);
        return new ListTemplatesResult(response);
    }

    private final Pattern mFilePattern = Pattern.compile("([-d])\\S+(\\S+\\s+){8}(.*)$");

    private List<String> parseTemplateList(byte[] response) {
        String responseString = new String(response);

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseString);

        List<String> lineList = new ArrayList<>();

        while (matcher.find()) {
            lineList.add(matcher.group());
        }

        List<String> templateList = new ArrayList<>();

        for (String fileString : lineList) {
            Matcher fileMatcher = mFilePattern.matcher(fileString);
            if (fileMatcher.find()) {
                String typeString = fileMatcher.group(1);
                String name = fileMatcher.group(3);
                if (typeString.equals("d")) {
                    continue;
                }
                templateList.add(name);
            }
        }

        return templateList;
    }

    private Response submitTemplate(NSubject subject, String cardPath) throws IOException {
        mGKCard.connect();
        ByteArrayInputStream inputStream = getTemplateInputStream(subject);
        Response response = mGKCard.put(cardPath, inputStream);
        if (response.getStatus() != 226) {
            return response;
        }
        return mGKCard.finalize(cardPath);
    }

    @NonNull
    private ByteArrayInputStream getTemplateInputStream(NSubject subject) {
        NTemplate template = null;
        try {
            template = subject.getTemplate();
            NLRecord faceRecord = template.getFaces().getRecords().get(0);
            byte[] buffer = faceRecord.save().toByteArray();
            return new ByteArrayInputStream(buffer);
        } catch (NullPointerException e) {
            return new ByteArrayInputStream(new byte[0]);
        } finally {
            if (template != null) {
                template.dispose();
            }
        }
    }

    public class ListTemplatesResult extends AuthResult {
        public static final String UNKNOWN_TEMPLATE = "UNKNOWN_TEMPLATE";

        protected final List<Object> mTemplates;

        public ListTemplatesResult(Response response) {
            super(response);
            mTemplates = parseTemplates();
        }

        public List<Object> getTemplates() {
            return mTemplates;
        }

        private List<Object> parseTemplates() {
            List<Object> list = new ArrayList<>();
            if (mStatus == Status.UNAUTHORIZED) {
                list.add(UNKNOWN_TEMPLATE);
            } else {
                if (mResponse.getData() == null) {
                    return list;
                }
                List<String> templates = parseTemplateList(mResponse.getData());
                for (String template : templates) {
                    if (template.startsWith("face")) {
                        list.add(template);
                    }
                }
            }
            return list;
        }
    }

    public class AuthResult {
        protected final Response mResponse;
        protected final Status mStatus;

        public AuthResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(mResponse);
        }

        public Status getStatus() {
            return mStatus;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 213:
                return Status.SUCCESS;
            case 226:
                return Status.SUCCESS;
            case 230:
                return Status.SIGNED_IN;
            case 231:
                return Status.SIGNED_OUT;
            case 250:
                return Status.SUCCESS;
            case 426:
                return Status.CANCELED;
            case 430:
                return Status.SIGN_IN_FAILURE;
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
