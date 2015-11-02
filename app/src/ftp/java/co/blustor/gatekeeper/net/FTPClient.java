package co.blustor.gatekeeper.net;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.OutputStream;

public interface FTPClient {
    FTPFile[] listFiles(String pathname) throws IOException;
    boolean setFileType(int filetype) throws IOException;
    void enterLocalPassiveMode();
    boolean retrieveFile(String remote, OutputStream local) throws IOException;
    boolean isConnected();
    void connect(String hostname) throws IOException;
    boolean login(String username, String password) throws IOException;
    void disconnect() throws IOException;
}
