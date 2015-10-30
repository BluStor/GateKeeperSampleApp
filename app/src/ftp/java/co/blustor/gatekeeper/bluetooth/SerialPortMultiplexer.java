package co.blustor.gatekeeper.bluetooth;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortMultiplexer {
    public final static int MAX_PORT_NUMBER = 2;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ByteArrayOutputStream[] mPortBuffers;
    private SerialPortPacketBuilder mSerialPortPacketBuilder;

    public SerialPortMultiplexer(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mPortBuffers = new ByteArrayOutputStream[MAX_PORT_NUMBER + 1];
        for(int i = 0; i < MAX_PORT_NUMBER; i++) {
            mPortBuffers[i] = new ByteArrayOutputStream();
        }
        mSerialPortPacketBuilder = new SerialPortPacketBuilder();
    }

    public void write(byte[] data, int port) throws IOException {
        SerialPortPacket packet = new SerialPortPacket(data, port);
        mOutputStream.write(packet.getBytes());
    }

    public int read(byte[] data, int port) throws IOException {
        int bytesRead = 0;
        int totalRead = 0;
        while(totalRead < data.length && bytesRead != -1) {
            bufferNextPacket();
            bytesRead = readFromBuffer(data, bytesRead, data.length - bytesRead, port);
            if(bytesRead != -1)
                totalRead += bytesRead;
        }

        return totalRead;
    }

    private int readFromBuffer(byte[] data, int off, int len, int port) throws IOException {
        ByteArrayOutputStream baos = mPortBuffers[port];
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        int bytesRead = bais.read(data, off, len);
        byte[] remainingBytes = new byte[bais.available()];
        bais.read(remainingBytes);
        mPortBuffers[port] = new ByteArrayOutputStream();
        mPortBuffers[port].write(remainingBytes);

        return bytesRead;
    }

    private void bufferNextPacket() throws IOException {
        SerialPortPacket packet = mSerialPortPacketBuilder.buildFromInputStream(mInputStream);
        ByteArrayOutputStream baos = mPortBuffers[packet.getPort()];
        baos.write(packet.getPayload());
    }


}
