package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import co.blustor.gatekeeper.data.GKBluetoothMultiplexer;
import co.blustor.gatekeeper.utils.GKStringUtils;

public class GKBluetoothCard implements GKCard {
    public static final String TAG = GKBluetoothCard.class.getSimpleName();

    private static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String LIST = "LIST";
    private static final String RETR = "RETR";
    private static final String STOR = "STOR";
    private static final String DELE = "DELE";
    private static final String MKD = "MKD";
    private static final String RMD = "RMD";
    private static final String SRFT = "SRFT";

    private static final int UPLOAD_DELAY_MILLIS = 6;

    private final BluetoothDevice mBluetoothDevice;
    private GKBluetoothMultiplexer mMultiplexer;

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
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                return commandResponse;
            }

            byte[] buffer = new byte[GKBluetoothMultiplexer.MAXIMUM_PAYLOAD_SIZE];
            while (inputStream.read(buffer, 0, buffer.length) != -1) {
                mMultiplexer.writeToDataChannel(buffer);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            Response dataResponse = getCommandResponse();
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(STOR, cardPath, e);
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
    public Response finalize(String cardPath) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        return call(SRFT, timestamp + " " + cardPath);
    }

    @Override
    public void connect() throws IOException {
        if (mMultiplexer == null) {
            try {
                BluetoothSocket socket = mBluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
                mMultiplexer = new GKBluetoothMultiplexer(socket);
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
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                return commandResponse;
            }

            Response dataResponse = getCommandResponse();
            byte[] data = mMultiplexer.readDataChannel();
            dataResponse.setData(data);
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            return new AbortResponse();
        }
    }

    private Response call(String method, String cardPath) throws IOException {
        try {
            sendCommand(method, cardPath);
            return getCommandResponse();
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            return new AbortResponse();
        }
    }

    private void sendCommand(String method, String argument) throws IOException {
        String cmd = buildCommandString(method, argument);
        Log.i(TAG, "Sending Command: '" + cmd.trim() + "'");
        byte[] bytes = getCommandBytes(cmd);
        mMultiplexer.writeToCommandChannel(bytes);
    }

    private Response getCommandResponse() throws IOException, InterruptedException {
        Response response = new Response(mMultiplexer.readCommandChannelLine());
        Log.i(TAG, "Card Response: '" + response.getStatusMessage() + "'");
        return response;
    }

    private byte[] getCommandBytes(String cmd) {
        return (cmd + "\r\n").getBytes(StandardCharsets.US_ASCII);
    }

    private String buildCommandString(String method, String... arguments) {
        return String.format("%s %s", method, GKStringUtils.join(arguments, " "));
    }

    private String globularPath(String cardPath) {
        if (cardPath.equals("/")) {
            cardPath += "*";
        } else {
            cardPath += "/*";
        }
        return cardPath;
    }

    private void logCommandInterruption(String method, String cardPath, InterruptedException e) {
        String commandString = buildCommandString(method, cardPath);
        Log.e(TAG, "'" + commandString + "' interrupted", e);
    }
}
