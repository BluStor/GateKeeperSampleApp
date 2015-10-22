package co.blustor.gatekeeper.net;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeeper.data.File;
import co.blustor.gatekeeper.data.IOConnection;

public class FTPFilestore implements IOConnection {
    private final FTPClient mFTP;

    public FTPFilestore() {
        mFTP = new FTPClient();
    }

    public List<File> listFiles(String targetPath) throws IOException {
        org.apache.commons.net.ftp.FTPFile[] files = mFTP.listFiles(targetPath);
        ArrayList<File> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                result.add(new FTPFile(files[i]));
            }
        }
        return result;
    }

    @Override
    public void open() throws IOException {
        if (!mFTP.isConnected()) {
            mFTP.connect("192.168.0.43");
            mFTP.login("ftp", "guy@example.com");
        }
    }

    @Override
    public void close() throws IOException {
        if (mFTP.isConnected()) {
            mFTP.disconnect();
        }
    }

    private class FTPFile implements File {
        private final org.apache.commons.net.ftp.FTPFile mFile;

        public FTPFile(org.apache.commons.net.ftp.FTPFile file) {
            mFile = file;
        }

        @Override
        public String getName() {
            return mFile.getName();
        }

        @Override
        public Type getType() {
            return mFile.isDirectory() ? Type.DIRECTORY : Type.FILE;
        }
    }
}
