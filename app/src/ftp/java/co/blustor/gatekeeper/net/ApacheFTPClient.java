package co.blustor.gatekeeper.net;


import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ApacheFTPClient implements FTPClient {
    private org.apache.commons.net.ftp.FTPClient mFTPClient;

    public ApacheFTPClient() {
        mFTPClient = new org.apache.commons.net.ftp.FTPClient();
    }

    @Override
    public FTPFile[] listFiles(String pathname) throws IOException {
        return mFTPClient.listFiles(pathname);
    }

    @Override
    public boolean setFileType(int filetype) throws IOException {
        return mFTPClient.setFileType(filetype);
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
}
