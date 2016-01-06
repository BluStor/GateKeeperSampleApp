package co.blustor.gatekeeper.data;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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

    public byte[] readDataChannel() throws IOException, InterruptedException {
        List<Byte> byteList = new ArrayList<>();
        mChannelBuffers[DATA_CHANNEL].drainTo(byteList);
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
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
        byte[] packetBytes = DataPacketBuilder.toPacketBytes(data, channel);
        mOutputStream.write(packetBytes);
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
                    Log.e(TAG, "Exception occurred while buffering a DataPacket", e);
                    return;
                } catch (InterruptedException e) {
                    Log.e(TAG, "ChannelBuffer interrupted", e);
                    return;
                }
            }
        }

        private void bufferNextPacket() throws IOException, InterruptedException {
            DataPacket packet = DataPacketBuilder.build(mInputStream);
            BlockingQueue<Byte> buffer = mChannelBuffers[packet.getChannel()];
            byte[] bytes = packet.getPayload();
            for (int i = 0; i < bytes.length; i++) {
                buffer.put(bytes[i]);
            }
        }
    }

    private static class DataPacket {
        public static final int HEADER_SIZE = 3;
        public static final int CHECKSUM_SIZE = 2;

        public static final byte MOST_SIGNIFICANT_BIT = 0x00;
        public static final byte LEAST_SIGNIFICANT_BIT = 0x00;

        private byte[] mPayload;
        private int mChannel;

        public DataPacket(byte[] payload, int channel) {
            mPayload = payload;
            mChannel = channel;
        }

        public byte[] getPayload() {
            return mPayload;
        }

        public int getChannel() {
            return mChannel;
        }
    }

    private static class DataPacketBuilder {
        public static final String TAG = DataPacketBuilder.class.getSimpleName();

        public static DataPacket build(InputStream inputStream) throws IOException {
            byte[] header = readHeader(inputStream);
            int packetSize = getPacketSize(header);
            int channel = getPacketChannel(header);
            byte[] payload = readPayload(inputStream, packetSize);
            byte[] checksum = readChecksum(inputStream);

            return new DataPacket(payload, channel);
        }

        public static byte[] toPacketBytes(byte[] data, int channel) {
            int packetSize = data.length + 5;
            byte channelByte = getChannelByte(channel);
            byte msb = getMSB(packetSize);
            byte lsb = getLSB(packetSize);

            byte[] packet = new byte[data.length + 5];
            packet[0] = channelByte;
            packet[1] = msb;
            packet[2] = lsb;
            for (int i = 0; i < data.length; i++) {
                packet[i + 3] = data[i];
            }

            packet[packet.length - 2] = DataPacket.MOST_SIGNIFICANT_BIT;
            packet[packet.length - 1] = DataPacket.LEAST_SIGNIFICANT_BIT;
            return packet;
        }

        private static int getPacketSize(byte[] header) {
            byte packetSizeMSB = header[1];
            byte packetSizeLSB = header[2];
            int packetSize = (int) packetSizeMSB << 8;
            packetSize += (int) packetSizeLSB & 0xFF;
            return packetSize;
        }

        private static int getPacketChannel(byte[] header) {
            return (int) header[0];
        }

        private static byte[] readHeader(InputStream inputStream) throws IOException {
            return fillByteArrayFromStream(inputStream, DataPacket.HEADER_SIZE);
        }

        private static byte[] readPayload(InputStream inputStream, int packetSize) throws IOException {
            int payloadsize = packetSize - (DataPacket.HEADER_SIZE + DataPacket.CHECKSUM_SIZE);
            return fillByteArrayFromStream(inputStream, payloadsize);
        }

        private static byte[] readChecksum(InputStream inputStream) throws IOException {
            return fillByteArrayFromStream(inputStream, DataPacket.CHECKSUM_SIZE);
        }

        private static byte[] fillByteArrayFromStream(InputStream inputStream, int length) throws IOException {
            byte[] data = new byte[length];
            int totalBytesRead = 0;
            int bytesRead = 0;
            while (totalBytesRead < length && bytesRead != -1) {
                bytesRead = inputStream.read(data, totalBytesRead, length - totalBytesRead);
                if (bytesRead != -1) {
                    totalBytesRead += bytesRead;
                }
            }
            return data;
        }

        private static byte getChannelByte(int channel) {
            return (byte) (channel & 0xff);
        }

        private static byte getMSB(int size) {
            return (byte) (size >> 8);
        }

        private static byte getLSB(int size) {
            return (byte) (size & 0xff);
        }
    }
}
