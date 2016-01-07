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
    private static final int DEMO_TEMPLATE_INDEX = 1;
    private static final String DEMO_TEMPLATE_ASSET_NAME = "DemoTemplate.dat";

    private final Context mContext;

    public DemoAuthentication(GKCard gkCard, Context context) {
        super(gkCard);
        mContext = context;
    }

    public AuthResult enrollWithDemoFace() throws IOException {
        NSubject subject = getDemoSubject();
        return enrollWithFace(subject, DEMO_TEMPLATE_INDEX);
    }

    public AuthResult signInWithDemoFace() throws IOException {
        NSubject subject = getDemoSubject();
        return signInWithFace(subject);
    }

    public Status revokeDemoFace() throws IOException {
        return revokeFace(DEMO_TEMPLATE_INDEX);
    }

    @Override
    public ListTemplatesResult listTemplates() throws IOException {
        ListTemplatesResult result = super.listTemplates();
        if (result.getStatus() == Status.UNAUTHORIZED) {
            signInWithDemoFace();
            return super.listTemplates();
        }
        return result;
    }

    private NSubject getDemoSubject() throws IOException {
        AssetManager assets = mContext.getAssets();
        InputStream templateStream = assets.open(DEMO_TEMPLATE_ASSET_NAME);
        try {
            byte[] bytes = getTemplateBytes(templateStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            return NSubject.fromMemory(byteBuffer);
        } finally {
            templateStream.close();
        }
    }

    private byte[] getTemplateBytes(InputStream stream) throws IOException {
        int bytesRead;
        int bufferByteCount = 1024;
        byte[] buffer;

        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        buffer = new byte[bufferByteCount];
        while ((bytesRead = stream.read(buffer, 0, bufferByteCount)) != -1) {
            outputByteStream.write(buffer, 0, bytesRead);
        }
        return outputByteStream.toByteArray();
    }
}
