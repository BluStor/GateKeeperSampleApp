package co.blustor.gatekeeper.data;

import android.content.Context;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NFile;

import java.io.File;
import java.io.IOException;

public class DroidDatastore implements Datastore {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static DroidDatastore mInstance;
    private static Context mContext;

    private DroidDatastore(Context context) {
        mContext = context;
    }

    public static DroidDatastore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DroidDatastore(context);
        }
        return mInstance;
    }

    public void storeTemplate(NSubject subject) throws IOException {
        String dataPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
        File outputFile = new File(dataPath, "gatekeeper_capture.dat");
        if (outputFile.exists()) {
            outputFile.delete();
        } else {
            outputFile.getParentFile().mkdirs();
        }
        NFile.writeAllBytes(outputFile.getAbsolutePath(), subject.getTemplateBuffer());
    }
}
