package co.blustor.gatekeeper.data;

import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DroidFilestore {
    public java.io.File makeTempPath() throws IOException {
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String uniqueDir = UUID.randomUUID().toString();
        File path = new File(downloadsPath, uniqueDir);
        path.mkdir();
        return path;
    }

    public static class CachedFile extends AbstractFile {
        private Uri mUri;

        public CachedFile(java.io.File file) {
            super(file.getName(), Type.FILE);
            mUri = Uri.fromFile(file);
        }

        public Uri getUri() {
            return mUri;
        }
    }
}
