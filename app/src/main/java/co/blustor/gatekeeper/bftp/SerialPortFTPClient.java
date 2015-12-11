package co.blustor.gatekeeper.bftp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.blustor.gatekeeper.ftp.FTPClient;
import co.blustor.gatekeeper.ftp.FTPFile;

public class SerialPortFTPClient implements FTPClient {
    public final static String TAG = SerialPortFTPClient.class.getSimpleName();

    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;

    private final static int UPLOAD_DELAY_MILLIS = 6;

    SerialPortMultiplexer mSerialPortMultiplexer;

    public SerialPortFTPClient(SerialPortMultiplexer multiplexer) {
        mSerialPortMultiplexer = multiplexer;
    }

    @Override
    public FTPFile[] listFiles(String pathname) throws IOException {
        sendCommandLIST(pathname);

        ReadDataThread readDataThread = new ReadDataThread(mSerialPortMultiplexer);
        Thread t = new Thread(readDataThread);
        try {
            getReply();
            t.start();
            getReply();
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
            getReply();
            ReadDataThread readDataThread = new ReadDataThread(mSerialPortMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            getReply();
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
    public boolean storeFile(String remote, InputStream local) throws IOException {
        sendCommandSTOR(remote);
        try {
            getReply();
            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while (local.read(buffer, 0, buffer.length) != -1) {
                mSerialPortMultiplexer.write(buffer, DATA_CHANNEL);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            getReply();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to STOR a file.", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to STOR a file.", e);
        }
        return false;
    }

    @Override
    public boolean deleteFile(String fileAbsolutePath) throws IOException {
        sendCommandDELE(fileAbsolutePath);
        try {
            getReply();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to DELE a file.", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to DELE a file.", e);
        }
        return false;
    }

    @Override
    public boolean removeDirectory(String directoryAbsolutePath) throws IOException {
        sendCommandRMD(directoryAbsolutePath);
        try {
            String replyString = getReply();
            if (replyString.equals("250 RMD command successful")) {
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to RMD a directory.", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to RMD a directory.", e);
        }
        return false;
    }

    @Override
    public boolean makeDirectory(String directoryAbsolutePath) throws IOException {
        sendCommandMKD(directoryAbsolutePath);
        try {
            String replyString = getReply();
            if (replyString.equals("257 Directory created")) {
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to MKD a directory.", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to MKD a directory.", e);
        }
        return false;
    }

    private void sendCommandLIST(String directory) throws IOException {
        if (directory.equals("/")) {
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

    private void sendCommand(String FTPCommand, String argument) throws IOException {
        String cmd = String.format("%s %s\r\n", FTPCommand, argument);
        Log.i(TAG, "FTP Command: " + cmd);
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mSerialPortMultiplexer.write(bytes, COMMAND_CHANNEL);
    }

    private String getReply() throws IOException, InterruptedException {
        byte[] line = mSerialPortMultiplexer.readLine(COMMAND_CHANNEL);
        String reply = new String(line);
        Log.i(TAG, "FTP Reply: " + reply);
        return reply;
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
            while (true) {
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
