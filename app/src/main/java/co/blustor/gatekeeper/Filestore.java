package co.blustor.gatekeeper;

import android.content.Context;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NFile;

import java.io.File;
import java.io.IOException;

public class Filestore {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static Filestore mInstance;
    private static Context mContext;

    private Filestore(Context context) {
        mContext = context;
    }

    public static Filestore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Filestore(context);
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
