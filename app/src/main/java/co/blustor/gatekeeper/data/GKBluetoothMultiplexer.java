package co.blustor.gatekeeper.data;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GKBluetoothMultiplexer {
    public static final String TAG = GKBluetoothMultiplexer.class.getSimpleName();

    public static final int MAXIMUM_PAYLOAD_SIZE = 256;
    public static final int COMMAND_CHANNEL = 1;
    public static final int DATA_CHANNEL = 2;
    public static final int MAX_CHANNEL_NUMBER = 2;

    private static final byte CARRIAGE_RETURN = 13;
    private static final byte LINE_FEED = 10;

    private final BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BlockingQueue<Byte>[] mChannelBuffers = new LinkedBlockingQueue[MAX_CHANNEL_NUMBER + 1];
    private SerialPortPacketBuilder mSerialPortPacketBuilder = new SerialPortPacketBuilder();
    private Thread mBufferingThread;

    {
        for (int i = 0; i <= MAX_CHANNEL_NUMBER; i++) {
            mChannelBuffers[i] = new LinkedBlockingQueue<>();
        }
    }

    public GKBluetoothMultiplexer(BluetoothSocket socket) {
        mSocket = socket;
    }

    public void writeToCommandChannel(byte[] data) throws IOException {
        write(data, COMMAND_CHANNEL);
    }

    public void writeToDataChannel(byte[] data) throws IOException {
        write(data, DATA_CHANNEL);
    }

    public byte[] readCommandChannelLine() throws IOException, InterruptedException {
        return readLine(COMMAND_CHANNEL);
    }

    public int readDataChannel(byte[] data) throws IOException, InterruptedException {
        return read(data, DATA_CHANNEL);
    }

    public void connect() throws IOException {
        mSocket.connect();
        mInputStream = mSocket.getInputStream();
        mOutputStream = mSocket.getOutputStream();
        mBufferingThread = new Thread(new ChannelBuffer());
        mBufferingThread.start();
    }

    public void disconnect() throws IOException {
        mBufferingThread.interrupt();
        mInputStream.close();
        mOutputStream.close();
        if (mSocket != null) {
            mSocket.close();
        }
    }

    private void write(byte[] data, int channel) throws IOException {
        SerialPortPacket packet = new SerialPortPacket(data, channel);
        mOutputStream.write(packet.getBytes());
    }

    private byte[] readLine(int channel) throws IOException, InterruptedException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte a = read(channel);
        byte b = read(channel);
        while (a != CARRIAGE_RETURN && b != LINE_FEED) {
            bytes.write(a);
            a = b;
            b = read(channel);
        }
        return bytes.toByteArray();
    }

    private byte read(int channel) throws IOException, InterruptedException {
        byte[] buffer = new byte[1];
        read(buffer, channel);
        return buffer[0];
    }

    private int read(byte[] data, int channel) throws IOException, InterruptedException {
        int bytesRead = 0;
        int totalRead = 0;
        while (totalRead < data.length && bytesRead != -1) {
            bytesRead = readFromBuffer(data, bytesRead, data.length - bytesRead, channel);
            if (bytesRead != -1) {
                totalRead += bytesRead;
            }
        }
        return totalRead;
    }

    private int readFromBuffer(byte[] data, int off, int len, int channel) throws IOException, InterruptedException {
        BlockingQueue<Byte> buffer = mChannelBuffers[channel];
        int bytesRead = 0;
        for (int i = 0; i < len; i++) {
            data[off + i] = buffer.take();
            bytesRead = i;
        }
        return bytesRead + 1;
    }

    private class ChannelBuffer implements Runnable {
        public void run() {
            while (true) {
                try {
                    bufferNextPacket();
                } catch (IOException e) {
                    Log.e(TAG, "Exception occurred while buffering a SerialPortPacket", e);
                    return;
                } catch (InterruptedException e) {
                    Log.e(TAG, "ChannelBuffer interrupted", e);
                    return;
                }
            }
        }

        private void bufferNextPacket() throws IOException, InterruptedException {
            SerialPortPacket packet = mSerialPortPacketBuilder.buildFromInputStream(mInputStream);
            BlockingQueue<Byte> buffer = mChannelBuffers[packet.getChannel()];
            byte[] bytes = packet.getPayload();
            for (int i = 0; i < bytes.length; i++) {
                buffer.put(bytes[i]);
            }
        }
    }

    private static class SerialPortPacket {
        public static final int HEADER_SIZE = 3;
        public static final int CHECKSUM_SIZE = 2;

        private static final byte MOST_SIGNIFICANT_BIT = 0x00;
        private static final byte LEAST_SIGNIFICANT_BIT = 0x00;

        private byte[] mBytes;
        private int mPort;

        public SerialPortPacket(byte[] payload, int port) {
            int packetSize = payload.length + 5;
            mPort = port;
            byte portByte = getPortByte(port);
            byte msb = getMSB(packetSize);
            byte lsb = getLSB(packetSize);

            byte[] packet = new byte[payload.length + 5];
            packet[0] = portByte;
            packet[1] = msb;
            packet[2] = lsb;
            for (int i = 0; i < payload.length; i++) {
                packet[i + 3] = payload[i];
            }

            setPacketChecksum(packet);

            mBytes = packet;
        }

        private byte getPortByte(int port) {
            return (byte) (port & 0xff);
        }

        private byte getMSB(int size) {
            return (byte) (size >> 8);
        }

        private byte getLSB(int size) {
            return (byte) (size & 0xff);
        }

        private void setPacketChecksum(byte[] packet) {
            packet[packet.length - 2] = MOST_SIGNIFICANT_BIT;
            packet[packet.length - 1] = LEAST_SIGNIFICANT_BIT;
        }

        public byte[] getBytes() {
            return mBytes;
        }

        public int getChannel() {
            return mPort;
        }

        public byte[] getPayload() {
            return Arrays.copyOfRange(mBytes, HEADER_SIZE, mBytes.length - CHECKSUM_SIZE);
        }
    }

    private static class SerialPortPacketBuilder {
        public static final String TAG = SerialPortPacketBuilder.class.getSimpleName();

        public SerialPortPacket buildFromInputStream(InputStream is) throws IOException {
            byte[] header = readHeader(is);
            int packetSize = getPacketSize(header);
            int port = getPacketPort(header);
            byte[] payload = readPayload(is, packetSize);
            byte[] checksum = readChecksum(is);

            return new SerialPortPacket(payload, port);
        }

        private int getPacketSize(byte[] header) {
            byte packetSizeMSB;
            byte packetSizeLSB;
            packetSizeMSB = header[1];
            packetSizeLSB = header[2];
            int packetSize = (int) packetSizeMSB << 8;
            packetSize += (int) packetSizeLSB & 0xFF;
            return packetSize;
        }

        private int getPacketPort(byte[] header) {
            return (int) header[0];
        }

        private byte[] readHeader(InputStream is) throws IOException {
            return fillByteArrayFromStream(is, SerialPortPacket.HEADER_SIZE);
        }

        private byte[] readPayload(InputStream is, int packetSize) throws IOException {
            int payloadsize = packetSize - (SerialPortPacket.HEADER_SIZE + SerialPortPacket.CHECKSUM_SIZE);
            return fillByteArrayFromStream(is, payloadsize);
        }

        private byte[] readChecksum(InputStream is) throws IOException {
            return fillByteArrayFromStream(is, SerialPortPacket.CHECKSUM_SIZE);
        }

        private byte[] fillByteArrayFromStream(InputStream is, int length) throws IOException {
            byte[] data = new byte[length];
            int totalBytesRead = 0;
            int bytesRead = 0;
            while (totalBytesRead < length && bytesRead != -1) {
                bytesRead = is.read(data, totalBytesRead, length - totalBytesRead);
                if (bytesRead != -1) {
                    totalBytesRead += bytesRead;
                }
            }
            return data;
        }
    }
}
