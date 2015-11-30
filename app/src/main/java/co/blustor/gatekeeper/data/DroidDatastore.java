package co.blustor.gatekeeper.data;

import android.content.Context;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NFile;

import java.io.File;
import java.io.IOException;

public class DroidDatastore implements Datastore {
    public static final String TAG = DroidDatastore.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DATA_PATH =
            android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                    FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";
    private static final String TEMPLATE_NAME = "gatekeeper_capture.dat";

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
        File outputFile = new File(DATA_PATH, TEMPLATE_NAME);
        if (outputFile.exists()) {
            outputFile.delete();
        } else {
            outputFile.getParentFile().mkdirs();
        }
        NFile.writeAllBytes(outputFile.getAbsolutePath(), subject.getTemplateBuffer());
    }

    public void deleteTemplate() {
        File templateFile = new File(DATA_PATH, TEMPLATE_NAME);
        if (templateFile.exists())
            templateFile.delete();
    }

    public NSubject getTemplate() throws IOException {
        File templateFile = new File(DATA_PATH, TEMPLATE_NAME);
        if (templateFile.exists()) {
            NSubject subject = NSubject.fromFile(templateFile.getAbsolutePath());
            subject.setId(templateFile.getAbsolutePath());
            return subject;
        } else {
            return new NSubject();
        }
    }

    public boolean hasTemplate() {
        File templateFile = new File(DATA_PATH, TEMPLATE_NAME);
        return templateFile.exists();
    }
}
