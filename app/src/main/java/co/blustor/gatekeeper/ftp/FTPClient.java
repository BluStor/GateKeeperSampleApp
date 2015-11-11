package co.blustor.gatekeeper.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.blustor.gatekeeper.bftp.FTPProtocolConstants;

public interface FTPClient {
    FTPFile[] listFiles(String pathname) throws IOException;
    boolean setFileType(FTPProtocolConstants.DATA_TYPE dataType) throws IOException;
    void enterLocalPassiveMode();
    boolean retrieveFile(String remote, OutputStream local) throws IOException;
    boolean isConnected();
    void connect(String hostname) throws IOException;
    boolean login(String username, String password) throws IOException;
    void disconnect() throws IOException;
    boolean storeFile(String remote, InputStream local) throws IOException;
    boolean deleteFile(String pathname) throws IOException;
    boolean removeDirectory(String pathname) throws IOException;
    boolean makeDirectory(String directoryAbsolutePath) throws IOException;
}
