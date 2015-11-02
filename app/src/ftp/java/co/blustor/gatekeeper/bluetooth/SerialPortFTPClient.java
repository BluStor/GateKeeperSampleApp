package co.blustor.gatekeeper.bluetooth;


import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        String cmd = "LIST ";
        if(pathname.equals("/"))
            pathname += "*";
        else
            pathname += "/*";
        cmd += pathname + "\r\n";
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mSerialPortMultiplexer.write(bytes, COMMAND_CHANNEL);

        ReadDataThread readDataThread = new ReadDataThread(mSerialPortMultiplexer);
        Thread t = new Thread(readDataThread);
        try {
            byte[] line = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            String command = new String(line);
            Log.e(TAG, command);
            t.start();
            line = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            command = new String(line);
            Log.e(TAG, command);
            t.interrupt();

            FTPResponseParser parser = new FTPResponseParser();

            FTPFile[] files = parser.parseListResponse(readDataThread.getData());

            return files;
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted exception from listFiles...?");
            e.printStackTrace();
            return null;
        }
    }

    public boolean setFileType(int filetype) {
        // Done
        return true;
    }

    public void enterLocalPassiveMode() {
        // Done
    }

    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        String cmd = "RETR " + remote + "\r\n";
        Log.e(TAG, cmd);

        mSerialPortMultiplexer.write(cmd.getBytes(StandardCharsets.US_ASCII), COMMAND_CHANNEL);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));

            ReadDataThread readDataThread = new ReadDataThread(mSerialPortMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));

            t.interrupt();
            byte[] fileData = readDataThread.getData();
            local.write(fileData);

            return true;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException during retrieveFile.");
            e.printStackTrace();
            return false;
        }
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
            while(true) {
                try {
                    multiplexer.read(b, DATA_CHANNEL);
                    data.write(b[0]);
                } catch (IOException e) {
                    Log.e(TAG, "IOException in ReadDataThread while trying to read byte from DataChannel.");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public byte[] getData() {
            return data.toByteArray();
        }
    }
}
