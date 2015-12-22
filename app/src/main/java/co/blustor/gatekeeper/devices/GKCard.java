package co.blustor.gatekeeper.devices;

import java.io.IOException;
import java.io.InputStream;

public interface GKCard {
    Response list(String cardPath) throws IOException;
    Response get(String cardPath) throws IOException;
    Response put(String cardPath, InputStream inputStream) throws IOException;
    Response delete(String cardPath) throws IOException;
    Response createPath(String cardPath) throws IOException;
    Response deletePath(String cardPath) throws IOException;
    void connect() throws IOException;
    void disconnect() throws IOException;

    class Response {
        protected int mStatus;
        protected String mMessage;
        protected byte[] mData;

        public Response(int status, String message) {
            mStatus = status;
            mMessage = message;
        }

        public Response(byte[] commandData) {
            this(commandData, null);
        }

        public Response(byte[] commandData, byte[] bodyData) {
            String responseString = new String(commandData);
            String[] split = responseString.split("\\s", 2);
            mStatus = Integer.parseInt(split[0]);
            mMessage = split[1];
            mData = bodyData;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getStatusMessage() {
            return mStatus + " " + mMessage;
        }

        public byte[] getData() {
            return mData;
        }
    }
}
