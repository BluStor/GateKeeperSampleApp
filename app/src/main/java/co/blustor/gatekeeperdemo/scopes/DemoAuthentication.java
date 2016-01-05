package co.blustor.gatekeeperdemo.scopes;

import android.content.Context;
import android.content.res.AssetManager;

import com.neurotec.biometrics.NSubject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;

public class DemoAuthentication extends GKAuthentication {
    private static final int TEST_TEMPLATE_INDEX = 1;

    private static final String DEMO_TEMPLATE_ASSET_NAME = "DemoTemplate.dat";

    private final Context mContext;

    public DemoAuthentication(GKCard gkCard, Context context) {
        super(gkCard);
        mContext = context;
    }

    public Status enrollWithTestFace() throws IOException {
        NSubject subject = getDemoSubject();
        return enrollWithFace(subject, TEST_TEMPLATE_INDEX);
    }

    public Status signInWithTestFace() throws IOException {
        NSubject subject = getDemoSubject();
        return signInWithFace(subject);
    }

    public Status revokeTestFace() throws IOException {
        return revokeFace(TEST_TEMPLATE_INDEX);
    }

    @Override
    public ListTemplatesResponse listTemplates() throws IOException {
        GKCard.Response response = mGKCard.list(LIST_FACE_PATH);
        if (response.getStatus() == 530) {
            signInWithTestFace();
        }
        return super.listTemplates();
    }

    private NSubject getDemoSubject() throws IOException {
        AssetManager assets = mContext.getAssets();
        InputStream templateStream = assets.open(DEMO_TEMPLATE_ASSET_NAME);
        try {
            byte[] bytes = getBytes(templateStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            return NSubject.fromMemory(byteBuffer);
        } finally {
            templateStream.close();
        }
    }

    public byte[] getBytes(InputStream stream) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        while ((len = stream.read(buf, 0, size)) != -1) {
            bos.write(buf, 0, len);
        }
        return bos.toByteArray();
    }
}
