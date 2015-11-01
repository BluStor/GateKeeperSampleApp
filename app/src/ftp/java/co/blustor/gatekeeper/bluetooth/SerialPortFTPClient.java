package co.blustor.gatekeeper.bluetooth;


import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayOutputStream;
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

        ReadDataThread readDataThread = new ReadDataThread(mSerialPortMultiplexer);
        Thread t = new Thread(readDataThread);
        byte[] line = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
        String command = new String(line);
        Log.e(TAG, command);
        t.start();
        line = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
        command = new String(line);
        Log.e(TAG, command);
        t.interrupt();
        String data = new String(readDataThread.getData());
        Log.e(TAG, data);

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

    private class ReadDataThread implements Runnable {
        private ByteArrayOutputStream data;
        private SerialPortMultiplexer multiplexer;

        public ReadDataThread(SerialPortMultiplexer serialPortMultiplexer) {
            data = new ByteArrayOutputStream();
            multiplexer = serialPortMultiplexer;
        }

        public void run() {
            byte[] b = new byte[1];
            while(!Thread.interrupted()) {
                try {
                    multiplexer.read(b, DATA_CHANNEL);
                    data.write(b[0]);
                } catch (IOException e) {
                    Log.e(TAG, "IOException in ReadDataThread while trying to read byte from DataChannel.");
                    e.printStackTrace();
                }
            }
        }

        public byte[] getData() {
            return data.toByteArray();
        }
    }
}
