package co.blustor.gatekeeper.devices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.apps.filevault.VaultFile;
import co.blustor.gatekeeper.apps.filevault.VaultFile.Type;
import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.ftp.FTPFile;

public class GKBluetoothCard implements GKCard {
    public final static String TAG = GKBluetoothCard.class.getSimpleName();

    private final CardClient mClient;

    public GKBluetoothCard(CardClient client) {
        mClient = client;
    }

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        FTPFile[] files = mClient.listFiles(targetPath);
        ArrayList<VaultFile> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                result.add(new FTPVaultFile(targetPath, files[i]));
            }
        }
        return result;
    }

    @Override
    public File downloadFile(VaultFile vaultFile) throws IOException {
        File targetFile = vaultFile.getLocalPath();
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        mClient.retrieveFile(vaultFile.getRemotePath(), outputStream);
        return targetFile;
    }

    @Override
    public boolean uploadFile(String targetPath, InputStream localFile) throws IOException {
        return mClient.storeFile(targetPath, localFile);
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        return mClient.deleteFile(fileAbsolutePath);
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        return mClient.makeDirectory(directoryAbsolutePath);
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        return mClient.removeDirectory(directoryAbsolutePath);
    }

    @Override
    public String getRootPath() {
        return "/";
    }

    private class FTPVaultFile extends VaultFile {
        public FTPVaultFile(String targetPath, FTPFile file) {
            super(file.getName(), getFileType(file));
            setRemotePath(targetPath, file.getName());
        }
    }

    private Type getFileType(FTPFile file) {
        return file.isDirectory() ? Type.DIRECTORY : Type.FILE;
    }
}
