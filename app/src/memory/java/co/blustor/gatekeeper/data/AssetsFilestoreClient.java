package co.blustor.gatekeeper.data;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.Application;
import co.blustor.gatekeeper.util.FileUtils;

public class AssetsFilestoreClient implements RemoteFilestoreClient {
    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        ArrayList<VaultFile> files = new ArrayList<>();
        Context context = Application.getAppContext();
        String[] filenames = context.getAssets().list(targetPath);
        for (String name : filenames) {
            if (name.contains(".")) {
                files.add(new VaultFile(name, VaultFile.Type.FILE));
            } else {
                files.add(new VaultFile(name, VaultFile.Type.DIRECTORY));
            }
        }
        return files;
    }

    @Override
    public File downloadFile(String remotePath, File targetFile) throws IOException {
        Context context = Application.getAppContext();
        InputStream inputStream = context.getAssets().open(remotePath);
        FileUtils.writeStreamToFile(inputStream, targetFile);
        return targetFile;
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
}
