package co.blustor.gatekeeper.bftp;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.blustor.gatekeeper.devices.GKCard.Response;

public class CardClient {
    public final static String TAG = CardClient.class.getSimpleName();

    public static final String LIST = "LIST";
    public static final String RETR = "RETR";
    public static final String STOR = "STOR";
    public static final String DELE = "DELE";
    public static final String MKD = "MKD";
    public static final String RMD = "RMD";

    private final static int UPLOAD_DELAY_MILLIS = 6;

    private BluetoothSocket mSocket;
    private IOMultiplexer mMultiplexer;

    public CardClient(BluetoothSocket socket) {
        mSocket = socket;
    }

    @NonNull
    public Response get(String method, String cardPath) throws IOException {
        try {
            sendCommand(method, cardPath);
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
            Log.e(TAG, method + " '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response put(String cardPath, InputStream inputStream) throws IOException {
        try {
            sendCommand(STOR, cardPath);
            Response response = getCommandResponse();
            if (response.getStatus() == 530) {
                return response;
            }

            byte[] buffer = new byte[SerialPortPacket.MAXIMUM_PAYLOAD_SIZE];
            while (inputStream.read(buffer, 0, buffer.length) != -1) {
                mMultiplexer.writeToDataChannel(buffer);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            byte[] commandBytes = getCommandBytes();
            return new Response(commandBytes);
        } catch (InterruptedException e) {
            Log.e(TAG, STOR + " '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public Response call(String method, String cardPath) throws IOException {
        try {
            sendCommand(method, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, method + " '" + cardPath + "' interrupted", e);
            return new AbortResponse();
        }
    }

    public void connect() throws IOException {
        mSocket.connect();
        InputStream is = mSocket.getInputStream();
        OutputStream os = mSocket.getOutputStream();
        mMultiplexer = new IOMultiplexer(is, os);
    }

    public void disconnect() throws IOException {
        mMultiplexer.close();
        if (mSocket != null) {
            mSocket.close();
        }
    }

    private void sendCommand(String method, String argument) throws IOException {
        String cmd = String.format("%s %s\r\n", method, argument);
        Log.i(TAG, "Sending Command: " + cmd);
        byte[] bytes = cmd.getBytes(StandardCharsets.US_ASCII);
        mMultiplexer.writeToCommandChannel(bytes);
    }

    private Response getCommandResponse() throws IOException, InterruptedException {
        Response response = new Response(getCommandBytes());
        Log.i(TAG, "Card Response: " + response.getStatusMessage());
        return response;
    }

    private byte[] getCommandBytes() throws IOException, InterruptedException {
        return mMultiplexer.readCommandChannelLine();
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
                    multiplexer.readDataChannel(b);
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
