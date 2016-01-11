package co.blustor.gatekeeper.biometrics;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class GKFaces {
    private final NBiometricClient mBiometricClient;

    public GKFaces() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.initialize();
        mBiometricClient.setFacesTemplateSize(NTemplateSize.SMALL);
    }

    public Template createTemplateFromBitmap(Bitmap bitmap) {
        NSubject subject = new NSubject();
        NImage nImage = NImage.fromBitmap(bitmap);
        NFace nFace = mBiometricClient.detectFaces(nImage);
        nFace.setImage(nImage);
        subject.getFaces().add(nFace);
        NBiometricStatus status = mBiometricClient.createTemplate(subject);
        return new Template(subject, status);
    }

    public Template createTemplateFromStream(InputStream inputStream) throws IOException {
        try {
            byte[] bytes = getTemplateBytes(inputStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            NSubject subject = NSubject.fromMemory(byteBuffer);
            return new Template(subject, NBiometricStatus.OK);
        } catch (UnsupportedOperationException e) {
            return new Template(Template.Quality.BAD_DATA);
        }
    }

    private byte[] getTemplateBytes(InputStream stream) throws IOException {
        int bytesRead;
        int bufferByteCount = 1024;
        byte[] buffer;

        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        buffer = new byte[bufferByteCount];
        while ((bytesRead = stream.read(buffer, 0, bufferByteCount)) != -1) {
            outputByteStream.write(buffer, 0, bytesRead);
        }
        return outputByteStream.toByteArray();
    }

    public static class Template {
        public enum Quality {
            OK,
            BLURRY,
            NO_FACE,
            BAD_DATA
        }

        private final NSubject mSubject;
        private final Quality mQuality;

        private Template(Quality quality) {
            mSubject = null;
            mQuality = quality;
        }

        private Template(NSubject subject, NBiometricStatus biometricStatus) {
            mSubject = subject;
            mQuality = parseQuality(biometricStatus);
        }

        public Quality getQuality() {
            return mQuality;
        }

        @NonNull
        public InputStream getInputStream() {
            NTemplate template = null;
            try {
                template = mSubject.getTemplate();
                NLRecord faceRecord = template.getFaces().getRecords().get(0);
                byte[] buffer = faceRecord.save().toByteArray();
                return new ByteArrayInputStream(buffer);
            } catch (NullPointerException e) {
                return new ByteArrayInputStream(new byte[0]);
            } finally {
                if (template != null) {
                    template.dispose();
                }
            }
        }

        private Quality parseQuality(NBiometricStatus biometricStatus) {
            switch (biometricStatus) {
                case OK:
                    return Quality.OK;
                case BAD_SHARPNESS:
                    return Quality.BLURRY;
                default:
                    return Quality.NO_FACE;
            }
        }
    }
}
