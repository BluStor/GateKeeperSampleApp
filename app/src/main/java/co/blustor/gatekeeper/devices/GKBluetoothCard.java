package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import co.blustor.gatekeeper.bftp.IOMultiplexer;

public class GKBluetoothCard implements GKCard {
    public final static String TAG = GKBluetoothCard.class.getSimpleName();

    private static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String LIST = "LIST";
    private static final String RETR = "RETR";
    private static final String STOR = "STOR";
    private static final String DELE = "DELE";
    private static final String MKD = "MKD";
    private static final String RMD = "RMD";

    private static final int UPLOAD_DELAY_MILLIS = 6;

    private final BluetoothDevice mBluetoothDevice;
    private IOMultiplexer mMultiplexer;

    public GKBluetoothCard(BluetoothDevice device) {
        mBluetoothDevice = device;
    }

    @Override
    public Response list(String cardPath) throws IOException {
        cardPath = globularPath(cardPath);
        return get(LIST, cardPath);
    }

    @Override
    public Response get(String cardPath) throws IOException {
        return get(RETR, cardPath);
    }

    @Override
    public Response put(String cardPath, InputStream inputStream) throws IOException {
        try {
            sendCommand(STOR, cardPath);
            Response response = getCommandResponse();
            if (response.getStatus() == 530) {
                return response;
            }

            byte[] buffer = new byte[IOMultiplexer.MAXIMUM_PAYLOAD_SIZE];
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

    @Override
    public Response delete(String cardPath) throws IOException {
        return call(DELE, cardPath);
    }

    @Override
    public Response createPath(String cardPath) throws IOException {
        return call(MKD, cardPath);
    }

    @Override
    public Response deletePath(String cardPath) throws IOException {
        return call(RMD, cardPath);
    }

    @Override
    public void connect() throws IOException {
        if (mMultiplexer == null) {
            try {
                BluetoothSocket socket = mBluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
                mMultiplexer = new IOMultiplexer(socket);
                mMultiplexer.connect();
            } catch (IOException e) {
                mMultiplexer = null;
                throw e;
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (mMultiplexer != null) {
            try {
                mMultiplexer.disconnect();
            } finally {
                mMultiplexer = null;
            }
        }
    }

    private Response get(String method, String cardPath) throws IOException {
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

    private Response call(String method, String cardPath) throws IOException {
        try {
            sendCommand(method, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            Log.e(TAG, method + " '" + cardPath + "' interrupted", e);
            return new AbortResponse();
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

    private String globularPath(String cardPath) {
        if (cardPath.equals("/")) {
            cardPath += "*";
        } else {
            cardPath += "/*";
        }
        return cardPath;
    }
}
