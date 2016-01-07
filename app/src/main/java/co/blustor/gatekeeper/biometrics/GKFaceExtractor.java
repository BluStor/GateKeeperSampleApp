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

public class GKFaceExtractor {
    private final NBiometricClient mBiometricClient;

    public GKFaceExtractor() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.initialize();
        mBiometricClient.setFacesTemplateSize(NTemplateSize.SMALL);
    }

    public Template createTemplateFromBitmap(Bitmap bitmap) {
        NSubject subject = new NSubject();
        NImage nImage = NImage.fromBitmap(bitmap);
        NFace nFace = new NFace();
        nFace.setImage(nImage);
        subject.getFaces().add(nFace);
        NBiometricStatus status = mBiometricClient.createTemplate(subject);
        if (status == NBiometricStatus.OK) {
            return new Template(subject);
        }
        return new NullTemplate();
    }

    public Template createTemplateFromStream(InputStream inputStream) throws IOException {
        byte[] bytes = getTemplateBytes(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        NSubject subject = NSubject.fromMemory(byteBuffer);
        return new Template(subject);
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

    public class Template {
        private final NSubject mSubject;

        private Template(NSubject subject) {
            mSubject = subject;
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
    }

    private class NullTemplate extends Template {
        private NullTemplate() {
            super(null);
        }
    }
}
