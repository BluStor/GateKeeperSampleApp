package co.blustor.gatekeeperdemo;

import java.io.File;


import co.blustor.gatekeeperdemo.filevault.LocalFilestore;
import co.blustor.gatekeepersdk.biometrics.GKFaces;
import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;

import static android.os.Environment.getExternalStorageDirectory;

public class Application extends android.app.Application {
    public static final String TAG = Application.class.getSimpleName();

    private static final String FIXED_DEVICE_NAME = "BLUSTOR";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static GKBluetoothCard sCard;
    private static GKFaces sFaces;

    public static GKCard getGKCard() {
        if (sCard == null) {
            sCard = new GKBluetoothCard(FIXED_DEVICE_NAME);
        }
        return sCard;
    }

    public static GKFaces getGKFaces() {
        if (sFaces == null) {
            sFaces = new GKFaces();
        }
        return sFaces;
    }

    public static LocalFilestore getLocalFilestore() {
        return new LocalFilestore(getCachePath());
    }

    private static File getCachePath() {
        return new File(getAppDataPath(), "_cache");
    }

    private static File getAppDataPath() {
        String basePath = getExternalStorageDirectory().getAbsolutePath();
        String dataPath = FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        return new File(basePath, dataPath);
    }
}
