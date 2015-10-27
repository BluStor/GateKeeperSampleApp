package co.blustor.gatekeeper.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
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
}
