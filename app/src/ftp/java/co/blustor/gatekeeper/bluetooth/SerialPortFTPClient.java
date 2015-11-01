package co.blustor.gatekeeper.bluetooth;


import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SerialPortFTPClient {
    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;
    private final static String TAG = "SerialPortFTPClient";

    SerialPortMultiplexer mSerialPortMultiplexer;

    public SerialPortFTPClient(SerialPortMultiplexer multiplexer) {
        mSerialPortMultiplexer = multiplexer;
    }

    public FTPFile[] listFiles(String pathname) throws IOException {
        String cmd = "LIST " + pathname + "*" + "\r\n";
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mSerialPortMultiplexer.write(bytes, COMMAND_CHANNEL);
        byte[] line;
        while((line = mSerialPortMultiplexer.readLine(DATA_CHANNEL)).length > 0) {

            Log.e(TAG, new String(line));
        }
        return new FTPFile[1];
    }

    public boolean setFileType(int filetype) {
        // Done
        return true;
    }

    public void enterLocalPassiveMode() {
        // Done
    }

    public boolean retrieveFile(String remote, OutputStream local) {
        return true;
    }

    public boolean isConnected() {
        // Done
        return true;
    }

    public void connect(String hostname) {
        // Done
    }

    public boolean login(String username, String password) {
        // Done
        return true;
    }

    public void disconnect() {
        // Done
    }
}
