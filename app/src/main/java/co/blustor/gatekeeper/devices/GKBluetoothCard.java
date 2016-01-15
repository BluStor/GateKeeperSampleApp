package co.blustor.gatekeeper.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

    private static final int UPLOAD_DELAY_MILLIS = 1;

    private final String mCardName;
    private GKBluetoothMultiplexer mMultiplexer;
    private List<Monitor> mCardMonitors = new ArrayList<>();

    private GKCard.ConnectionState mConnectionState = GKCard.ConnectionState.DISCONNECTED;

    public GKBluetoothCard(String cardName) {
        mCardName = cardName;
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
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(STOR, cardPath);
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                onConnectionChanged(ConnectionState.CONNECTED);
                return commandResponse;
            }

            byte[] buffer = new byte[GKBluetoothMultiplexer.MAXIMUM_PAYLOAD_SIZE];
            while (inputStream.read(buffer, 0, buffer.length) != -1) {
                mMultiplexer.writeToDataChannel(buffer);
                Thread.sleep(UPLOAD_DELAY_MILLIS);
            }
            Response dataResponse = getCommandResponse();
            onConnectionChanged(ConnectionState.CONNECTED);
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(STOR, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
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
            onConnectionChanged(ConnectionState.CONNECTING);
            BluetoothDevice bluetoothDevice = findBluetoothDevice();
            if (bluetoothDevice == null) {
                return;
            }
            try {
                BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
                mMultiplexer = new GKBluetoothMultiplexer(socket, this);
                mMultiplexer.connect();
            } catch (IOException e) {
                mMultiplexer = null;
                onConnectionChanged(ConnectionState.DISCONNECTED);
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

    @Override
    public ConnectionState getConnectionState() {
        synchronized (mCardMonitors) {
            return mConnectionState;
        }
    }

    @Override
    public void onConnectionChanged(ConnectionState state) {
        synchronized (mCardMonitors) {
            if (mConnectionState.equals(state)) {
                return;
            }
            mConnectionState = state;
            if (state.equals(ConnectionState.DISCONNECTING) || state.equals(ConnectionState.DISCONNECTED)) {
                mMultiplexer = null;
            }
            for (Monitor monitor : mCardMonitors) {
                monitor.onStateChanged(state);
            }
        }
    }

    @Override
    public void addMonitor(Monitor monitor) {
        synchronized (mCardMonitors) {
            if (!mCardMonitors.contains(monitor)) {
                mCardMonitors.add(monitor);
            }
        }
    }

    @Override
    public void removeMonitor(Monitor monitor) {
        synchronized (mCardMonitors) {
            if (mCardMonitors.contains(monitor)) {
                mCardMonitors.remove(monitor);
            }
        }
    }

    private Response get(String method, String cardPath) throws IOException {
        try {
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(method, cardPath);
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                onConnectionChanged(ConnectionState.CONNECTED);
                return commandResponse;
            }

            Response dataResponse = getCommandResponse();
            byte[] data = mMultiplexer.readDataChannel();
            dataResponse.setData(data);
            onConnectionChanged(ConnectionState.CONNECTED);
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
            return new AbortResponse();
        }
    }

    private Response call(String method, String cardPath) throws IOException {
        try {
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(method, cardPath);
            Response commandResponse = getCommandResponse();
            onConnectionChanged(ConnectionState.CONNECTED);
            return commandResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
            return new AbortResponse();
        }
    }

    private void sendCommand(String method, String argument) throws IOException {
        checkMultiplexer();
        String cmd = buildCommandString(method, argument);
        Log.i(TAG, "Sending Command: '" + cmd.trim() + "'");
        byte[] bytes = getCommandBytes(cmd);
        mMultiplexer.writeToCommandChannel(bytes);
    }

    private Response getCommandResponse() throws IOException, InterruptedException {
        checkMultiplexer();
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

    private void checkMultiplexer() throws IOException {
        if (mMultiplexer == null) {
            throw new IOException("Not Connected");
        }
    }

    private void logCommandInterruption(String method, String cardPath, InterruptedException e) {
        String commandString = buildCommandString(method, cardPath);
        Log.e(TAG, "'" + commandString + "' interrupted", e);
    }

    @Nullable
    private BluetoothDevice findBluetoothDevice() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (!adapter.isEnabled()) {
            onConnectionChanged(ConnectionState.BLUETOOTH_DISABLED);
            return null;
        }
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(mCardName)) {
                return device;
            }
        }
        onConnectionChanged(ConnectionState.CARD_NOT_PAIRED);
        return null;
    }

    @NonNull
    private BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new RuntimeException("Bluetooth is not available on this device");
        }
        return adapter;
    }
}
