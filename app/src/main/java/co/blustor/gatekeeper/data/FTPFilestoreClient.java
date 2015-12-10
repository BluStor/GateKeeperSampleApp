package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.bftp.FTPProtocolConstants;
import co.blustor.gatekeeper.data.VaultFile.Type;
import co.blustor.gatekeeper.ftp.FTPClient;
import co.blustor.gatekeeper.ftp.FTPFile;

public class FTPFilestoreClient implements RemoteFilestoreClient {
    public final static String TAG = FTPFilestoreClient.class.getSimpleName();

    private final FTPClient mFTP;

    public FTPFilestoreClient(FTPClient ftpClient) {
        mFTP = ftpClient;
    }

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        FTPFile[] files = mFTP.listFiles(targetPath);
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
        mFTP.setFileType(FTPProtocolConstants.DATA_TYPE.BINARY);
        mFTP.enterLocalPassiveMode();
        File targetFile = vaultFile.getLocalPath();
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        mFTP.retrieveFile(vaultFile.getRemotePath(), outputStream);
        return targetFile;
    }

    @Override
    public boolean uploadFile(String targetPath, InputStream localFile) throws IOException {
        return mFTP.storeFile(targetPath, localFile);
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        return mFTP.deleteFile(fileAbsolutePath);
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        return mFTP.removeDirectory(directoryAbsolutePath);
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        return mFTP.makeDirectory(directoryAbsolutePath);
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
