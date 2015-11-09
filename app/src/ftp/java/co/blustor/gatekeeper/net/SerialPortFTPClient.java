package co.blustor.gatekeeper.net;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.blustor.gatekeeper.protocol.FTPProtocolConstants;
import co.blustor.gatekeeper.response.FTPResponseParser;
import co.blustor.gatekeeper.serialport.SerialPortMultiplexer;
import co.blustor.gatekeeper.serialport.SerialPortPacket;

public class SerialPortFTPClient implements co.blustor.gatekeeper.net.FTPClient {
    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;
    private final static String TAG = "SerialPortFTPClient";

    SerialPortMultiplexer mSerialPortMultiplexer;

    public SerialPortFTPClient(SerialPortMultiplexer multiplexer) {
        mSerialPortMultiplexer = multiplexer;
    }

    @Override
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

    @Override
    public boolean setFileType(FTPProtocolConstants.DATA_TYPE dataType) {
        return true;
    }

    @Override
    public void enterLocalPassiveMode() {
    }

    @Override
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

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void connect(String hostname) {
    }

    @Override
    public boolean login(String username, String password) {
        return true;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean storeFile(String remote, InputStream local) throws IOException {
        String cmd = "STOR " + remote + "\r\n";
        Log.e(TAG, "FTP Command: " + cmd);

        mSerialPortMultiplexer.write(cmd.getBytes(StandardCharsets.US_ASCII), COMMAND_CHANNEL);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while(local.read(buffer, 0, buffer.length) != -1) {
                mSerialPortMultiplexer.write(buffer, DATA_CHANNEL);
                Thread.sleep(100);
            }
            reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to STOR a file.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to STOR a file.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        String cmd = "DELE " + fileAbsolutePath + "\r\n";
        Log.e(TAG, "FTP Command: " + cmd);

        mSerialPortMultiplexer.write(cmd.getBytes(StandardCharsets.US_ASCII), COMMAND_CHANNEL);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to DELE a file.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to DELE a file.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        String cmd = "RMD " + directoryAbsolutePath + "\r\n";
        Log.e(TAG, "FTP Command: " + cmd);

        mSerialPortMultiplexer.write(cmd.getBytes(StandardCharsets.US_ASCII), COMMAND_CHANNEL);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            String replyString = new String(reply);
            Log.e(TAG, "Reply: " + replyString);
            if(replyString.equals("250 RMD command successful")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to RMD a directory.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to RMD a directory.");
            e.printStackTrace();
            return false;
        }
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
