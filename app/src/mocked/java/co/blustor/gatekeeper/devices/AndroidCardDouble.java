package co.blustor.gatekeeper.devices;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient.Response;
import co.blustor.gatekeeper.util.FileUtils;

public class AndroidCardDouble implements GKCard {
    public static final String TAG = AndroidCardDouble.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DATA_PATH = android.os.Environment.getExternalStorageDirectory()
                                                                  .getAbsolutePath() +
            FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";

    private boolean mConnected;

    @Override
    public Response list(String cardPath) throws IOException {
        List<String> lines = listFiles(cardPath);
        ArrayList<byte[]> bytes = new ArrayList<>();
        int length = 0;
        for (String line : lines) {
            byte[] lineBytes = line.getBytes();
            length += lineBytes.length;
            bytes.add(lineBytes);
        }
        int startPos = 0;
        byte[] result = new byte[length];
        for (byte[] lineBytes : bytes) {
            System.arraycopy(lineBytes, 0, result, startPos, lineBytes.length);
            startPos += lineBytes.length;
        }
        return new Response("226 Success".getBytes(), result);
    }

    @Override
    public Response retrieve(String cardPath) throws IOException {
        File file = new File(DATA_PATH, fullPath(cardPath));
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        try {
            buf.read(bytes, 0, bytes.length);
            return new Response("226 Success".getBytes(), bytes);
        } catch (FileNotFoundException e) {
            return new Response("550 Not found.".getBytes(), new byte[0]);
        } finally {
            buf.close();
        }
    }

    @Override
    public Response store(String cardPath, InputStream localFile) {
        try {
            checkConnection();
            File targetFile = new File(DATA_PATH, fullPath(cardPath));
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            FileUtils.writeStreamToFile(localFile, targetFile);
        } catch (IOException e) {
            Log.e(TAG, "IO Error", e);
            return new Response(450, "IO Error");
        }
        return new Response(226, "Success");
    }

    @Override
    public Response delete(String cardPath) throws IOException {
        checkConnection();
        return new Response(250, "Success");
    }

    @Override
    public Response makeDirectory(String cardPath) throws IOException {
        checkConnection();
        File targetDirectory = new File(DATA_PATH, fullPath(cardPath));
        if (targetDirectory.mkdir()) {
            return new Response(250, "Success");
        } else {
            return new Response(550, "Not found.");
        }
    }

    @Override
    public Response removeDirectory(String cardPath) throws IOException {
        checkConnection();
        File targetDirectory = new File(DATA_PATH, fullPath(cardPath));
        if (targetDirectory.delete()) {
            return new Response(250, "Success");
        } else {
            return new Response(550, "Not found.");
        }
    }

    @Override
    public void connect() throws IOException {
        mConnected = true;
    }

    @Override
    public void disconnect() throws IOException {
        mConnected = false;
    }

    private List<String> listFiles(String cardPath) {
        String endLine = "\r\n";
        String otherInfo = "1 root root 100000 Oct 29 2015";
        File directory = new File(DATA_PATH, fullPath(cardPath));
        ArrayList<String> lines = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                String type = (file.isDirectory() ? "d" : "-") + "rw-rw-rw-";
                String name = file.getName();
                lines.add(type + " " + otherInfo + " " + name + endLine);
            }
        }
        return lines;
    }

    private String fullPath(String subPath) {
        return FileUtils.joinPath(FileUtils.ROOT, "ftp", subPath);
    }

    private void checkConnection() throws IOException {
        if (!mConnected) {
            throw new IOException("GKCard is not connected");
        }
    }
}
