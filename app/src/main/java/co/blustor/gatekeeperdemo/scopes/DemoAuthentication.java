package co.blustor.gatekeeperdemo.scopes;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.neurotec.biometrics.NSubject;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;

public class DemoAuthentication extends GKAuthentication {
    private static final int TEST_TEMPLATE_INDEX = 1;

    public DemoAuthentication(GKCard gkCard, Context context) {
        super(gkCard);
    }

    public Status enrollWithTestFace() throws IOException {
        NSubject subject = getTestSubject();
        return enrollWithFace(subject, TEST_TEMPLATE_INDEX);
    }

    public Status signInWithTestFace() throws IOException {
        NSubject subject = getTestSubject();
        return signInWithFace(subject);
    }

    public Status revokeTestFace() throws IOException {
        return revokeFace(TEST_TEMPLATE_INDEX);
    }

    private NSubject getTestSubject() throws IOException {
        String templatePath = getAbsolutePath("GoodTemplate.dat");
        return NSubject.fromFile(templatePath);
    }

    @Override
    public ListTemplatesResponse listTemplates() throws IOException {
        GKCard.Response response = mGKCard.list(LIST_FACE_PATH);
        if (response.getStatus() == 530) {
            signInWithTestFace();
        }
        return super.listTemplates();
    }

    @NonNull
    private String getAbsolutePath(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, filename).getAbsolutePath();
    }
}
