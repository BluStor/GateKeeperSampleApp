package co.blustor.gatekeeperdemo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.services.GKAuthentication;

public class DemoHelper {
    private static final String TAG = DemoHelper.class.getSimpleName();
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

    public Boolean cardHasCapturedEnrollment(GKCard card, GKFaces faces) throws IOException {
        GKAuthentication authentication = new GKAuthentication(card);
        GKAuthentication.ListTemplatesResult templateList = authentication.listTemplates();
        if (templateList.getTemplates().size() == 0) {
            addDemoTemplate(card, faces);
            return false;
        }
        if (templateList.getTemplates().contains("UNKNOWN_TEMPLATE")) {
            bypassAuthentication(card, faces);
            GKAuthentication.ListTemplatesResult templates = authentication.listTemplates();
            return templates.getTemplates().contains("0");
        }
        if (templateList.getTemplates().size() >= 2) {
            return true;
        }
        return false;
    }

    public void removeFaceTemplate(final GKCard card) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    new GKAuthentication(card).revokeFace();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to revoke face", e);
                }
                return null;
            }
        }.execute();
    }
}
