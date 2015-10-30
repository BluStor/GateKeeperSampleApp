package co.blustor.gatekeeper.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class SerialPortPacketBuilder {
    public SerialPortPacket buildFromInputStream(InputStream is) throws IOException {
        byte[] header = readHeader(is);
        int packetSize = getPacketSize(header);
        int port = getPacketPort(header);
        byte[] payload = readPayload(is, packetSize);
        // The checksum is discarded; the C server code requires the checksum but does not use it.
        byte[] checksum = readChecksum(is);

        return new SerialPortPacket(payload, port);
    }

    private int getPacketSize(byte[] header) {
        byte packetSizeMSB;
        byte packetSizeLSB;
        packetSizeMSB = header[1];
        packetSizeLSB = header[2];
        int packetSize = (int) packetSizeMSB << 8;
        packetSize += (int) packetSizeLSB;
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
        while(totalBytesRead < length && bytesRead != -1) {
            bytesRead = is.read(data, totalBytesRead, length - totalBytesRead);
            if(bytesRead != -1) {
                totalBytesRead += bytesRead;
            }
        }
        return data;
    }

}
