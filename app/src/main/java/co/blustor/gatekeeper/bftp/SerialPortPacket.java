package co.blustor.gatekeeper.bftp;

import java.util.Arrays;

public class SerialPortPacket {
    public static final int MAXIMUM_PAYLOAD_SIZE = 256;
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

    public int getPort() {
        return mPort;
    }

    public byte[] getPayload() {
        return Arrays.copyOfRange(mBytes, HEADER_SIZE, mBytes.length - CHECKSUM_SIZE);
    }
}
