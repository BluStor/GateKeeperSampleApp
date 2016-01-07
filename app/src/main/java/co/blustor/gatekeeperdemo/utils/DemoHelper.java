package co.blustor.gatekeeperdemo.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;

public class DemoHelper {
    private static final int DEMO_TEMPLATE_INDEX = 1;
    private static final String DEMO_TEMPLATE_ASSET_NAME = "DemoTemplate.dat";

    private final Context mContext;

    public DemoHelper(Context context) {
        mContext = context;
    }

    public GKAuthentication.AuthResult addDemoTemplate(GKCard card, GKFaces faces) throws IOException {
        GKFaces.Template demoTemplate = getDemoTemplate(faces);
        return new GKAuthentication(card).enrollWithFace(demoTemplate, DEMO_TEMPLATE_INDEX);
    }

    public GKAuthentication.AuthResult removeDemoTemplate(GKCard card) throws IOException {
        return new GKAuthentication(card).revokeFace(DEMO_TEMPLATE_INDEX);
    }

    public GKAuthentication.AuthResult bypassAuthentication(GKCard card, GKFaces faces) throws IOException {
        GKFaces.Template demoTemplate = getDemoTemplate(faces);
        return new GKAuthentication(card).signInWithFace(demoTemplate);
    }

    public GKAuthentication.AuthResult authenticateWithFile(String filename, GKCard card, GKFaces faces) throws IOException {
        GKFaces.Template demoTemplate = getTemplateFromFile(faces, filename);
        return new GKAuthentication(card).signInWithFace(demoTemplate);
    }

    private GKFaces.Template getDemoTemplate(GKFaces faces) throws IOException {
        return getTemplateFromFile(faces, DEMO_TEMPLATE_ASSET_NAME);
    }

    private GKFaces.Template getTemplateFromFile(GKFaces faces, String filename) throws IOException {
        AssetManager assets = mContext.getAssets();
        InputStream templateStream = assets.open(filename);
        try {
            return faces.createTemplateFromStream(templateStream);
        } finally {
            templateStream.close();
        }
    }
}
