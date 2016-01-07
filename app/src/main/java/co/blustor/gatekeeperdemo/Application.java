package co.blustor.gatekeeperdemo;

import android.content.Context;

import java.io.File;

import co.blustor.gatekeeperdemo.filevault.FileVault;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeperdemo.filevault.LocalFilestore;
import co.blustor.gatekeeper.devices.GKCard;

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

    public static GKAuthentication getAuthentication() {
        return sConfiguration.getAuthentication();
    }

    public static FileVault getFileVault() {
        GKCard card = sConfiguration.getGKCard();
        LocalFilestore localFilestore = new LocalFilestore(getCachePath());
        return new FileVault(localFilestore, card);
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
        GKAuthentication getAuthentication();
        GKCard getGKCard();
    }
}
