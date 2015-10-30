package co.blustor.gatekeeper.bluetooth;


import org.apache.commons.net.ftp.FTPFile;

import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortFTPClient {
    public SerialPortFTPClient(InputStream inputStream, OutputStream outputStream) {

    }

    public FTPFile[] listFiles(String pathname) {
        return new FTPFile[1];
    }

    public boolean setFileType(int filetype) {
        return true;
    }

    public void enterLocalPassiveMode() {

    }

    public boolean retrieveFile(String remote, OutputStream local) {
        return true;
    }

    public boolean isConnected() {
        return true;
    }

    public void connect(String hostname) {

    }

    public boolean login(String username, String password) {
        return true;
    }

    public void disconnect() {

    }
}
