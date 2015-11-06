package co.blustor.gatekeeper.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.Application;
import co.blustor.gatekeeper.util.FileUtils;

public class AssetsFilestoreClient implements RemoteFilestoreClient {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DATA_PATH =
            android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                    FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "GateKeeper";

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        ArrayList<VaultFile> files = new ArrayList<>();
        Context context = Application.getAppContext();
        String[] filenames = context.getAssets().list(targetPath);
        for (String name : filenames) {
            if (name.contains(".")) {
                files.add(new AssetVaultFile(targetPath, name, VaultFile.Type.FILE));
            } else {
                files.add(new AssetVaultFile(targetPath, name, VaultFile.Type.DIRECTORY));
            }
        }
        return files;
    }

    @Override
    public File downloadFile(VaultFile vaultFile) throws IOException {
        Context context = Application.getAppContext();
        InputStream inputStream = context.getAssets().open(vaultFile.getRemotePath());
        File targetFile = vaultFile.getLocalPath();
        FileUtils.writeStreamToFile(inputStream, targetFile);
        return targetFile;
    }

    @Override
    public boolean uploadFile(String targetPath, InputStream localFile) throws IOException {
        File targetFile = new File(DATA_PATH, targetPath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        FileUtils.writeStreamToFile(localFile, targetFile);
        return true;
    }

    @Override
    public String getRootPath() {
        return "ftp";
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    public class AssetVaultFile extends VaultFile {
        public AssetVaultFile(String parentPath, String fileName, Type type) {
            super(fileName, type);
            setRemotePath(parentPath, fileName);
        }
    }
}
