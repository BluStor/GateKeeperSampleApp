package co.blustor.gatekeeper.net;

import android.content.res.Resources;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.Application;
import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.bluetooth.SerialPortFTPClient;
import co.blustor.gatekeeper.data.VaultFile;
import co.blustor.gatekeeper.data.VaultFile.Type;
import co.blustor.gatekeeper.data.RemoteFilestoreClient;

public class FTPFilestoreClient implements RemoteFilestoreClient {
    private final SerialPortFTPClient mFTP;

    public FTPFilestoreClient(SerialPortFTPClient ftpClient) {
        mFTP = ftpClient;
    }

    @Override
    public List<VaultFile> listFiles(String targetPath) throws IOException {
        org.apache.commons.net.ftp.FTPFile[] files = mFTP.listFiles(targetPath);
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
        mFTP.setFileType(FTP.BINARY_FILE_TYPE);
        mFTP.enterLocalPassiveMode();
        File targetFile = vaultFile.getLocalPath();
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        mFTP.retrieveFile(vaultFile.getRemotePath(), outputStream);
        return targetFile;
    }

    @Override
    public String getRootPath() {
        return "/";
    }

    @Override
    public void open() throws IOException {
        if (!mFTP.isConnected()) {
            Resources resources = Application.getAppContext().getResources();
            String hostAddress = resources.getString(R.string.ftp_host_address);
            String username = resources.getString(R.string.ftp_username);
            String password = resources.getString(R.string.ftp_password);
            mFTP.connect(hostAddress);
            mFTP.login(username, password);
        }
    }

    @Override
    public void close() throws IOException {
        if (mFTP.isConnected()) {
            mFTP.disconnect();
        }
    }

    private class FTPVaultFile extends VaultFile {
        public FTPVaultFile(String targetPath, org.apache.commons.net.ftp.FTPFile file) {
            super(file.getName(), getFileType(file));
            setRemotePath(targetPath, file.getName());
        }
    }

    private Type getFileType(org.apache.commons.net.ftp.FTPFile file) {
        return file.isDirectory() ? Type.DIRECTORY : Type.FILE;
    }
}
