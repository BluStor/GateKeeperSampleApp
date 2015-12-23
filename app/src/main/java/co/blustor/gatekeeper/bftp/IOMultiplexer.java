package co.blustor.gatekeeper.bftp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IOMultiplexer {
    public static final String TAG = IOMultiplexer.class.getSimpleName();

    public final static int COMMAND_CHANNEL = 1;
    public final static int DATA_CHANNEL = 2;
    public static final int MAX_CHANNEL_NUMBER = 2;

    private static final byte CARRIAGE_RETURN = 13;
    private static final byte LINE_FEED = 10;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BlockingQueue<Byte>[] mChannelBuffers;
    private SerialPortPacketBuilder mSerialPortPacketBuilder;
    private Thread mBufferingThread;

    public IOMultiplexer(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mChannelBuffers = new LinkedBlockingQueue[MAX_CHANNEL_NUMBER + 1];
        for (int i = 0; i <= MAX_CHANNEL_NUMBER; i++) {
            mChannelBuffers[i] = new LinkedBlockingQueue<>();
        }
        mSerialPortPacketBuilder = new SerialPortPacketBuilder();
        mBufferingThread = new Thread(new BufferingThread());
        mBufferingThread.start();
    }

    public void writeToCommandChannel(byte[] data) throws IOException {
        write(data, COMMAND_CHANNEL);
    }

    public void writeToDataChannel(byte[] data) throws IOException {
        write(data, DATA_CHANNEL);
    }

    public void write(byte[] data, int channel) throws IOException {
        SerialPortPacket packet = new SerialPortPacket(data, channel);
        mOutputStream.write(packet.getBytes());
    }

    public byte[] readCommandChannelLine() throws IOException, InterruptedException {
        return readLine(COMMAND_CHANNEL);
    }

    public int readDataChannel(byte[] data) throws IOException, InterruptedException {
        return read(data, DATA_CHANNEL);
    }

    public void close() throws IOException {
        mBufferingThread.interrupt();
        mInputStream.close();
        mOutputStream.close();
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
            if (bytesRead != -1) { totalRead += bytesRead; }
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

    private class BufferingThread implements Runnable {
        public void run() {
            while (true) {
                try {
                    bufferNextPacket();
                } catch (IOException e) {
                    Log.e(TAG, "Exception occurred while buffering a SerialPortPacket", e);
                    return;
                } catch (InterruptedException e) {
                    Log.e(TAG, "BufferingThread interrupted", e);
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
}
