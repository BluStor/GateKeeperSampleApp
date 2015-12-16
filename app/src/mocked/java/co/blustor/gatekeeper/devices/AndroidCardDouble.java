package co.blustor.gatekeeper.devices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.util.FileUtils;

public class AndroidCardDouble implements GKCard {
    public static final String TAG = AndroidCardDouble.class.getSimpleName();

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DATA_PATH =
            android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                    FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";

    private boolean mConnected;

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        checkConnection();
        ArrayList<VaultFile> files = new ArrayList<>();
        File file = new File(DATA_PATH, targetPath);
        if (file.exists()) {
            String[] filenames = file.list();
            for (String name : filenames) {
                if (name.contains(".")) {
                    files.add(new AssetVaultFile(targetPath, name, VaultFile.Type.FILE));
                } else {
                    files.add(new AssetVaultFile(targetPath, name, VaultFile.Type.DIRECTORY));
                }
            }
        }
        return files;
    }

    @Override
    public File downloadFile(VaultFile vaultFile) throws IOException {
        checkConnection();
        File remoteFile = new File(DATA_PATH, vaultFile.getRemotePath());
        InputStream inputStream = new FileInputStream(remoteFile);
        File targetFile = vaultFile.getLocalPath();
        FileUtils.writeStreamToFile(inputStream, targetFile);
        return targetFile;
    }

    @Override
    public boolean uploadFile(String targetPath, InputStream localFile) throws IOException {
        checkConnection();
        File targetFile = new File(DATA_PATH, targetPath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        FileUtils.writeStreamToFile(localFile, targetFile);
        return true;
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

    public class AssetVaultFile extends VaultFile {
        public AssetVaultFile(String parentPath, String fileName, Type type) {
            super(fileName, type);
            setRemotePath(parentPath, fileName);
        }
    }

    private void checkConnection() throws IOException {
        if (!mConnected) {
            throw new IOException("GKCard is not connected");
        }
    }
}