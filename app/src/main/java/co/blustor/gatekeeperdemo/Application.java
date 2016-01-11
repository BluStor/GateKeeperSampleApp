package co.blustor.gatekeeperdemo;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCardConnector;
import co.blustor.gatekeeperdemo.filevault.FileVault;
import co.blustor.gatekeeperdemo.filevault.LocalFilestore;

import static android.os.Environment.getExternalStorageDirectory;

public class Application extends android.app.Application {
    public static final String TAG = Application.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static FileVault getFileVault() {
        GKCard card = getGKCard();
        LocalFilestore localFilestore = new LocalFilestore(getCachePath());
        return new FileVault(localFilestore, card);
    }

    public static GKCard getGKCard() {
        try {
            return GKCardConnector.find();
        } catch (IOException e) {
            Log.e(TAG, "Unable to find GateKeeper Bluetooth Card", e);
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
}
