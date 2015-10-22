package co.blustor.gatekeeper;

import android.content.Context;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NFile;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeeper.data.Filestore;

public class DroidFilestore implements Filestore {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static DroidFilestore mInstance;
    private static Context mContext;

    private DroidFilestore(Context context) {
        mContext = context;
    }

    public static DroidFilestore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DroidFilestore(context);
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
