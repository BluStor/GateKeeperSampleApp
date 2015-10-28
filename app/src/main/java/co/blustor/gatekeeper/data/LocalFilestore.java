package co.blustor.gatekeeper.data;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LocalFilestore {
    public java.io.File makeTempPath() throws IOException {
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String uniqueDir = UUID.randomUUID().toString();
        File path = new File(downloadsPath, uniqueDir);
        path.mkdir();
        return path;
    }
}
