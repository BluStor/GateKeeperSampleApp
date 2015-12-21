package co.blustor.gatekeeper.devices;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.data.GKFile;
import co.blustor.gatekeeper.util.FileUtils;

public class AndroidCardDouble implements GKCard {
    public static final String TAG = AndroidCardDouble.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DATA_PATH = android.os.Environment.getExternalStorageDirectory()
                                                                  .getAbsolutePath() +
            FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";

    private boolean mConnected;

    @Override
    public byte[] retrieve(String cardPath) {
        return new byte[0];
    }

    @Override
    public List<GKFile> listFiles(String targetPath) throws IOException {
        checkConnection();
        ArrayList<GKFile> files = new ArrayList<>();
        File file = new File(DATA_PATH, targetPath);
        if (file.exists()) {
            String[] filenames = file.list();
            for (String name : filenames) {
                GKFile.Type type = name.contains(".") ? GKFile.Type.FILE : GKFile.Type.DIRECTORY;
                GKFile gkFile = new GKFile(name, type);
                gkFile.setCardPath(targetPath, name);
                files.add(gkFile);
            }
        }
        return files;
    }

    @Override
    public File downloadFile(GKFile cardFile, File localFile) throws IOException {
        checkConnection();
        File androidFile = new File(DATA_PATH, cardFile.getCardPath());
        InputStream inputStream = new FileInputStream(androidFile);
        FileUtils.writeStreamToFile(inputStream, localFile);
        return localFile;
    }

    @Override
    public CardClient.Response store(String targetPath, InputStream localFile) {
        try {
            checkConnection();
            File targetFile = new File(DATA_PATH, targetPath);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            FileUtils.writeStreamToFile(localFile, targetFile);
        } catch (IOException e) {
            Log.e(TAG, "IO Error", e);
            return new CardClient.Response(450, "IO Error");
        }
        return new CardClient.Response(226, "Success");
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        checkConnection();
        File targetFile = new File(DATA_PATH, fileAbsolutePath);
        return targetFile.delete();
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        checkConnection();
        File targetDirectory = new File(DATA_PATH, directoryAbsolutePath);
        return targetDirectory.delete();
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        checkConnection();
        File targetDirectory = new File(DATA_PATH, directoryAbsolutePath);
        return targetDirectory.mkdir();
    }

    @Override
    public String getRootPath() {
        return "ftp";
    }

    @Override
    public void connect() throws IOException {
        mConnected = true;
    }

    @Override
    public void disconnect() throws IOException {
        mConnected = false;
    }

    private void checkConnection() throws IOException {
        if (!mConnected) {
            throw new IOException("GKCard is not connected");
        }
    }
}
