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

    public Response list(String cardPath) throws IOException {
        try {
            sendCommandLIST(cardPath);
            Response response = getCommandResponse();
            if (response.getStatus() == 530) {
                return response;
            }

            ReadDataThread readDataThread = new ReadDataThread(mMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            byte[] commandBytes = getCommandBytes();
            t.interrupt();
            byte[] data = readDataThread.getData();
            return new Response(commandBytes, data);
        } catch (InterruptedException e) {
            Log.e(TAG, "list '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response retrieve(String cardPath) throws IOException {
        try {
            sendCommand(RETR, cardPath);
            Response response = getCommandResponse();
            if (response.getStatus() == 530) {
                return response;
            }

            ReadDataThread readDataThread = new ReadDataThread(mMultiplexer);
            Thread t = new Thread(readDataThread);
            t.start();

            byte[] commandBytes = getCommandBytes();
            t.interrupt();
            byte[] data = readDataThread.getData();
            return new Response(commandBytes, data);
        } catch (InterruptedException e) {
            Log.e(TAG, "retrieve '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response store(String cardPath, InputStream inputStream) throws IOException {
        try {
            sendCommand(STOR, cardPath);
            Response response = getCommandResponse();
            if (response.getStatus() == 530) {
                return response;
            }

            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while (inputStream.read(buffer, 0, buffer.length) != -1) {
                mMultiplexer.write(buffer, DATA_CHANNEL);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            byte[] commandBytes = getCommandBytes();
            return new Response(commandBytes);
        } catch (InterruptedException e) {
            Log.e(TAG, "store '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response delete(String cardPath) throws IOException {
        try {
            sendCommand(DELE, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, "delete '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response makeDirectory(String cardPath) throws IOException {
        try {
            sendCommand(MKD, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, "makeDirectory '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response removeDirectory(String cardPath) throws IOException {
        try {
            sendCommand(RMD, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, "removeDirectory '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
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

    private Response getCommandResponse() throws IOException, InterruptedException {
        Response response = new Response(getCommandBytes());
        Log.i(TAG, "Card Response: " + response.getStatusMessage());
        return response;
    }

    private byte[] getCommandBytes() throws IOException, InterruptedException {
        return mMultiplexer.readLine(COMMAND_CHANNEL);
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

    private static class AbortResponse extends Response {
        public AbortResponse() {
            super(null);
            mStatus = 426;
            mMessage = "Aborted.";
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
