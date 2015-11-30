package co.blustor.gatekeeper.ftp;

import org.apache.commons.net.ftp.FTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.bftp.FTPProtocolConstants;

public class ApacheFTPClient implements FTPClient {
    public final static String TAG = ApacheFTPClient.class.getSimpleName();

    private org.apache.commons.net.ftp.FTPClient mFTPClient;

    public ApacheFTPClient() {
        mFTPClient = new org.apache.commons.net.ftp.FTPClient();
    }

    @Override
    public FTPFile[] listFiles(String pathname) throws IOException {
        org.apache.commons.net.ftp.FTPFile[] apacheFTPFiles = mFTPClient.listFiles(pathname);
        List<FTPFile> filesList = new ArrayList<>();

        for (int i = 0; i < apacheFTPFiles.length; i++) {
            org.apache.commons.net.ftp.FTPFile f = apacheFTPFiles[i];
            if (f != null) {
                String name = f.getName();
                FTPFile.TYPE type = f.isDirectory() ? FTPFile.TYPE.DIRECTORY : FTPFile.TYPE.FILE;
                filesList.add(new FTPFile(name, type));
            }
        }

        FTPFile[] filesArray = new FTPFile[filesList.size()];
        return filesList.toArray(filesArray);
    }

    @Override
    public boolean setFileType(FTPProtocolConstants.DATA_TYPE dataType) throws IOException {
        int fileType = FTP.BINARY_FILE_TYPE;
        return mFTPClient.setFileType(fileType);
    }

    @Override
    public void enterLocalPassiveMode() {
        mFTPClient.enterLocalPassiveMode();
    }

    @Override
    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        return mFTPClient.retrieveFile(remote, local);
    }

    @Override
    public boolean isConnected() {
        return mFTPClient.isConnected();
    }

    @Override
    public void connect(String hostname) throws IOException {
        mFTPClient.connect(hostname);
    }

    @Override
    public boolean login(String username, String password) throws IOException {
        return mFTPClient.login(username, password);
    }

    @Override
    public void disconnect() throws IOException {
        mFTPClient.disconnect();
    }

    @Override
    public boolean storeFile(String remote, InputStream local) throws IOException {
        return mFTPClient.storeFile(remote, local);
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        return mFTPClient.deleteFile(fileAbsolutePath);
    }

    @Override
    public boolean removeDirectory(String pathname) throws IOException {
        return mFTPClient.removeDirectory(pathname);
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        return mFTPClient.makeDirectory(directoryAbsolutePath);
    }
}
