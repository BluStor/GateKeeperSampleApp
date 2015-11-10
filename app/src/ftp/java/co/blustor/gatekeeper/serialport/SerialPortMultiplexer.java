package co.blustor.gatekeeper.serialport;



import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialPortMultiplexer {
    public static final String TAG = SerialPortMultiplexer.class.getSimpleName();
    public final static int MAX_PORT_NUMBER = 2;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BlockingQueue<Byte>[] mPortBuffers;
    private SerialPortPacketBuilder mSerialPortPacketBuilder;
    private Thread mBufferingThread;

    public SerialPortMultiplexer(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mPortBuffers = new LinkedBlockingQueue[MAX_PORT_NUMBER + 1];
        for(int i = 0; i <= MAX_PORT_NUMBER; i++) {
            mPortBuffers[i] = new LinkedBlockingQueue<Byte>();
        }
        mSerialPortPacketBuilder = new SerialPortPacketBuilder();

        mBufferingThread = new Thread(new BufferingThread());
        mBufferingThread.start();
    }

    public void write(byte[] data, int port) throws IOException {
        SerialPortPacket packet = new SerialPortPacket(data, port);
        mOutputStream.write(packet.getBytes());
    }

    public byte read(int port) throws IOException, InterruptedException {
        byte[] buffer = new byte[1];
        read(buffer, port);
        return buffer[0];
    }

    public int read(byte[] data, int port) throws IOException, InterruptedException {
        int bytesRead = 0;
        int totalRead = 0;
        while(totalRead < data.length && bytesRead != -1) {
            bytesRead = readFromBuffer(data, bytesRead, data.length - bytesRead, port);
            if(bytesRead != -1)
                totalRead += bytesRead;
        }

        return totalRead;
    }

    public byte[] readLine(int port) throws IOException, InterruptedException {
        final byte CR = 13;
        final byte LF = 10;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte a = read(port);
        byte b = read(port);

        while(a != CR && b != LF) {
            bytes.write(a);
            a = b;
            b = read(port);
        }

        return bytes.toByteArray();
    }

    public void close() throws IOException {
        mBufferingThread.interrupt();
        mInputStream.close();
        mOutputStream.close();
    }

    private int readFromBuffer(byte[] data, int off, int len, int port) throws IOException, InterruptedException {
        BlockingQueue<Byte> buffer = mPortBuffers[port];

        int bytesRead = 0;
        for(int i = 0; i < len; i++) {
            data[off + i] = buffer.take();
            bytesRead = i;
        }

        return bytesRead + 1;
    }

    private class BufferingThread implements Runnable {
        public void run() {
            while(true) {
                try {
                    bufferNextPacket();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in SerialPortMultiplexer while trying to buffer a packet.", e);
                    return;
                } catch (InterruptedException e) {
                    Log.e(TAG, "BufferingThread interrupted in SerialPortMultiplexer.", e);
                    return;
                }
            }
        }

        private void bufferNextPacket() throws IOException, InterruptedException {
            SerialPortPacket packet = mSerialPortPacketBuilder.buildFromInputStream(mInputStream);
            BlockingQueue<Byte> buffer = mPortBuffers[packet.getPort()];
            byte[] bytes = packet.getPayload();
            for(int i = 0; i < bytes.length; i++) {
                buffer.put(bytes[i]);
            }
        }
    }
}
