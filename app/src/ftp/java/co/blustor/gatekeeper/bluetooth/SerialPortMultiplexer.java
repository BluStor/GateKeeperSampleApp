package co.blustor.gatekeeper.bluetooth;



import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialPortMultiplexer {
    public static final String TAG = "SerialPortMultiplexer";
    public final static int MAX_PORT_NUMBER = 2;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BlockingQueue<Byte>[] mPortBuffers;
    private SerialPortPacketBuilder mSerialPortPacketBuilder;

    public SerialPortMultiplexer(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mPortBuffers = new LinkedBlockingQueue[MAX_PORT_NUMBER + 1];
        for(int i = 0; i <= MAX_PORT_NUMBER; i++) {
            mPortBuffers[i] = new LinkedBlockingQueue<Byte>();
        }
        mSerialPortPacketBuilder = new SerialPortPacketBuilder();

        Thread t = new Thread(new BufferingThread());
        t.start();
    }

    public void write(byte[] data, int port) throws IOException {
        SerialPortPacket packet = new SerialPortPacket(data, port);
        mOutputStream.write(packet.getBytes());
    }

    public byte read(int port) throws IOException {
        byte[] buffer = new byte[1];
        read(buffer, port);
        return buffer[0];
    }

    public int read(byte[] data, int port) throws IOException {
        int bytesRead = 0;
        int totalRead = 0;
        while(totalRead < data.length && bytesRead != -1) {
//            Log.e(TAG, "Try to buffer a packet...");
            //bufferNextPacket();
//            Log.e(TAG, "Buffered a packet.");
            bytesRead = readFromBuffer(data, bytesRead, data.length - bytesRead, port);
            if(bytesRead != -1)
                totalRead += bytesRead;

//            Log.e(TAG, "totalRead: " + totalRead);
//            Log.e(TAG, "bytesRead: " + bytesRead);
        }

        return totalRead;
    }

    public byte[] readLine(int port) throws IOException {
        final byte CR = 13;
        final byte LF = 10;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        Log.e(TAG, "Reading a...");
        byte a = read(port);
//        Log.e(TAG, "Read!");
        byte b = read(port);
//        Log.e(TAG, "a: " + a);
//        Log.e(TAG, "b: " + b);

        while(a != CR && b != LF) {
//            Log.e(TAG, "No end of line...");
            bytes.write(a);
            a = b;
            b = read(port);
        }

        return bytes.toByteArray();
    }

    private int readFromBuffer(byte[] data, int off, int len, int port) throws IOException {
        BlockingQueue<Byte> buffer = mPortBuffers[port];

        int bytesRead = 0;
        for(int i = 0; i < len; i++) {
            try {
                data[off + i] = buffer.take();
//                Log.e(TAG, "" + data[off + i]);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted trying to take a byte off buffer " + port);
                e.printStackTrace();
            }
            bytesRead = i;
        }

        return bytesRead + 1;
    }

//    private void bufferNextPacket() throws IOException {
//        SerialPortPacket packet = mSerialPortPacketBuilder.buildFromInputStream(mInputStream);
//        BlockingQueue<Byte> buffer = mPortBuffers[packet.getPort()];
//        byte[] bytes = packet.getPayload();
//        for(int i = 0; i < bytes.length; i++) {
//            try {
//                buffer.put(bytes[i]);
//            } catch (InterruptedException e) {
//                Log.e(TAG, "Interrupted while trying to put a byte on buffer " + packet.getPort());
//                e.printStackTrace();
//            }
//        }
//    }

    private class BufferingThread implements Runnable {
        public void run() {
            while(true) {
                try {
                    bufferNextPacket();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in SerialPortMultiplexer while trying to buffer a packet.");
                    e.printStackTrace();
                }
            }
        }

        private void bufferNextPacket() throws IOException {
            SerialPortPacket packet = mSerialPortPacketBuilder.buildFromInputStream(mInputStream);
            BlockingQueue<Byte> buffer = mPortBuffers[packet.getPort()];
            byte[] bytes = packet.getPayload();
            for(int i = 0; i < bytes.length; i++) {
                try {
                    buffer.put(bytes[i]);
                    //Log.e(TAG, "Buffered byte: " + (char) bytes[i] + " on port " + packet.getPort());
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while trying to put a byte on buffer " + packet.getPort());
                    e.printStackTrace();
                }
            }
        }
    }


}
