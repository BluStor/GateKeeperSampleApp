package co.blustor.gatekeeper.util;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GKFileUtils {
    public static final String TAG = GKFileUtils.class.getSimpleName();

    public static final String ROOT = "/";

    public static String joinPath(Object... paths) {
        ArrayList<String> list = nonblankPathSegments(paths);
        return GKStringUtils.join(list.toArray(), "/").replace("//", "/");
    }

    public static void writeStreamToFile(InputStream stream, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file.getAbsolutePath());
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = stream.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        if (output != null) {
            output.close();
        }
        if (stream != null) {
            stream.close();
        }
    }

    @NonNull
    private static ArrayList<String> nonblankPathSegments(Object[] paths) {
        ArrayList<String> list = new ArrayList<>();
        for (Object path : paths) {
            if (path != null && path != "") {
                list.add((String) path);
            }
        }
        return list;
    }
}
