package co.blustor.gatekeeper.demo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.apps.filevault.FileVault;
import co.blustor.gatekeeper.authentication.Authentication;
import co.blustor.gatekeeper.data.GKFileBrowser;
import co.blustor.gatekeeper.data.LocalFilestore;
import co.blustor.gatekeeper.data.GKCard;

import static android.os.Environment.getExternalStorageDirectory;

public class Application extends android.app.Application {
    public static final String TAG = Application.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static Context sContext;
    private static Configuration sConfiguration;

    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sConfiguration = new co.blustor.gatekeeper.demo.Configuration();
    }

    public static Context getAppContext() {
        return Application.sContext;
    }

    public static Authentication getAuthentication() {
        return sConfiguration.getAuthentication();
    }

    public static FileVault getFileVault() {
        try {
            GKCard remoteClient = sConfiguration.getGKCard();
            LocalFilestore localFilestore = new LocalFilestore(getCachePath());
            GKFileBrowser fileBrowser = new GKFileBrowser(remoteClient);
            return new FileVault(localFilestore, fileBrowser);
        } catch (IOException e) {
            Log.e(TAG, "Unable to create FileVault", e);
            return null;
        }
    }

    private static File getAppDataPath() {
        String basePath = getExternalStorageDirectory().getAbsolutePath();
        String dataPath = FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        return new File(basePath, dataPath);
    }

    private static File getCachePath() {
        return new File(getAppDataPath(), "_cache");
    }

    public interface Configuration {
        Authentication getAuthentication();
        GKCard getGKCard() throws IOException;
    }
}
