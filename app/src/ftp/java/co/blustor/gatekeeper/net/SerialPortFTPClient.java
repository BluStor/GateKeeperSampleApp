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
    public final static String TAG = SerialPortFTPClient.class.getSimpleName();

    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;

    private final static int UPLOAD_DELAY_MILLIS = 6;

    SerialPortMultiplexer mSerialPortMultiplexer;

    public SerialPortFTPClient(SerialPortMultiplexer multiplexer) {
        mSerialPortMultiplexer = multiplexer;
    }

    private void sendCommand(String FTPCommand, String argument) throws IOException {
        String cmd = String.format("%s %s\r\n", FTPCommand, argument);
        Log.e(TAG, "FTP Command: " + cmd);
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mSerialPortMultiplexer.write(bytes, COMMAND_CHANNEL);
    }

    private void sendCommandLIST(String directory) throws IOException {
        if(directory.equals("/")) {
            directory += "*";
        } else {
            directory += "/*";
        }
        sendCommand("LIST", directory);
    }

    private void sendCommandRETR(String file) throws IOException {
        sendCommand("RETR", file);
    }

    private void sendCommandSTOR(String file) throws IOException {
        sendCommand("STOR", file);
    }

    private void sendCommandDELE(String file) throws IOException {
        sendCommand("DELE", file);
    }

    private void sendCommandRMD(String directory) throws IOException {
        sendCommand("RMD", directory);
    }

    private void sendCommandMKD(String directory) throws IOException {
        sendCommand("MKD", directory);
    }

    @Override
    public FTPFile[] listFiles(String pathname) throws IOException {
        sendCommandLIST(pathname);

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
            Log.e(TAG, "Interrupted exception from listFiles...?", e);
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
        sendCommandRETR(remote);
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
            Log.e(TAG, "InterruptedException during retrieveFile.", e);
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
        sendCommandSTOR(remote);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while(local.read(buffer, 0, buffer.length) != -1) {
                mSerialPortMultiplexer.write(buffer, DATA_CHANNEL);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to STOR a file.", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to STOR a file.", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        sendCommandDELE(fileAbsolutePath);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            Log.e(TAG, "Reply: " + new String(reply));
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to DELE a file.", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to DELE a file.", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        sendCommandRMD(directoryAbsolutePath);
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
            Log.e(TAG, "IOException while trying to RMD a directory.", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to RMD a directory.", e);
            return false;
        }
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        sendCommandMKD(directoryAbsolutePath);
        try {
            byte[] reply = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
            String replyString = new String(reply);
            Log.e(TAG, "Reply: " + replyString);
            if(replyString.equals("257 Directory created")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to MKD a directory.", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to MKD a directory.", e);
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
                    Log.e(TAG, "IOException in ReadDataThread while trying to read byte from DataChannel.", e);
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
