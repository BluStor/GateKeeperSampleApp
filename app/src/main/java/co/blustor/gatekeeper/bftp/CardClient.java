package co.blustor.gatekeeper.bftp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CardClient {
    public final static String TAG = CardClient.class.getSimpleName();

    private static final String LIST = "LIST";
    private static final String RETR = "RETR";
    private static final String STOR = "STOR";
    private static final String DELE = "DELE";
    private static final String MKD = "MKD";
    private static final String RMD = "RMD";

    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;

    private final static int UPLOAD_DELAY_MILLIS = 6;

    private IOMultiplexer mMultiplexer;

    public CardClient(IOMultiplexer multiplexer) {
        mMultiplexer = multiplexer;
    }

    public byte[] list(String cardPath) throws IOException {
        sendCommandLIST(cardPath);

        ReadDataThread readDataThread = new ReadDataThread(mMultiplexer);
        Thread t = new Thread(readDataThread);
        try {
            getReply();
            t.start();
            getReply();
            t.interrupt();

            return readDataThread.getData();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted exception during list", e);
            return null;
        }
    }

    public boolean retrieveFile(String cardPath, OutputStream outputStream) throws IOException {
        sendCommand(RETR, cardPath);
        try {
            getReply();
            ReadDataThread readDataThread = new ReadDataThread(mMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            getReply();
            t.interrupt();
            byte[] fileData = readDataThread.getData();
            outputStream.write(fileData);

            return true;
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException during retrieveFile.", e);
            return false;
        }
    }

    public byte[] retrieve(String cardPath) throws IOException {
        sendCommand(RETR, cardPath);
        try {
            getReply();
            ReadDataThread readDataThread = new ReadDataThread(mMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            getReply();
            t.interrupt();
            return readDataThread.getData();
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException during retrieve", e);
            return null;
        }
    }

    public Response store(String cardPath, InputStream inputStream) {
        try {
            sendCommand(STOR, cardPath);
            getReply();
            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while (inputStream.read(buffer, 0, buffer.length) != -1) {
                mMultiplexer.write(buffer, DATA_CHANNEL);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            byte[] commandBytes = getCommandBytes();
            return new Response(commandBytes);
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to STOR a file.", e);
            return new ErrorResponse(e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to STOR a file.", e);
            return new ErrorResponse(e);
        }
    }

    public Response delete(String cardPath) throws IOException {
        try {
            sendCommand(DELE, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException while trying to DELE a file.", e);
            return null;
        }
    }

    public boolean deleteFile(String cardPath) throws IOException {
        sendCommand(DELE, cardPath);
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

    public boolean removeDirectory(String cardPath) throws IOException {
        sendCommand(RMD, cardPath);
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

    public boolean makeDirectory(String cardPath) throws IOException {
        sendCommand(MKD, cardPath);
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

    public void close() throws IOException {
        mMultiplexer.close();
    }

    private void sendCommandLIST(String cardPath) throws IOException {
        if (cardPath.equals("/")) {
            cardPath += "*";
        } else {
            cardPath += "/*";
        }
        sendCommand(LIST, cardPath);
    }

    private void sendCommand(String method, String argument) throws IOException {
        String cmd = String.format("%s %s\r\n", method, argument);
        Log.i(TAG, "FTP Command: " + cmd);
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mMultiplexer.write(bytes, COMMAND_CHANNEL);
    }

    private String getReply() throws IOException, InterruptedException {
        byte[] line = getCommandBytes();
        String reply = new String(line);
        Log.i(TAG, "FTP Reply: " + reply);
        return reply;
    }

    private byte[] getCommandBytes() throws IOException, InterruptedException {
        return mMultiplexer.readLine(COMMAND_CHANNEL);
    }

    private Response getCommandResponse() throws IOException, InterruptedException {
        return new Response(getCommandBytes());
    }

    public static class Response {
        protected int mStatus;
        protected String mMessage;
        protected byte[] mData;

        public Response(int status, String message) {
            mStatus = status;
            mMessage = message;
        }

        public Response(byte[] commandData) {
            this(commandData, null);
        }

        public Response(byte[] commandData, byte[] bodyData) {
            String responseString = new String(commandData);
            String[] split = responseString.split("\\s", 2);
            mStatus = Integer.parseInt(split[0]);
            mMessage = split[1];
            mData = bodyData;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getStatusMessage() {
            return mStatus + " " + mMessage;
        }

        public byte[] getData() {
            return mData;
        }
    }

    private static class ErrorResponse extends Response {
        public ErrorResponse(InterruptedException e) {
            super(null);
            mStatus = 426;
            mMessage = e.getMessage();
        }

        public ErrorResponse(IOException e) {
            super(null);
            mStatus = 450;
            mMessage = e.getMessage();
        }
    }

    private class ReadDataThread implements Runnable {
        private ByteArrayOutputStream data;
        private IOMultiplexer multiplexer;

        public ReadDataThread(IOMultiplexer ioMultiplexer) {
            data = new ByteArrayOutputStream();
            multiplexer = ioMultiplexer;
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
