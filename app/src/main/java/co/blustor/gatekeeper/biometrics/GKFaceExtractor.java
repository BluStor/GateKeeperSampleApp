package co.blustor.gatekeeper.biometrics;

import android.graphics.Bitmap;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;

public class GKFaceExtractor {
    private final NBiometricClient mBiometricClient;

    public GKFaceExtractor() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.initialize();
    }

    public NSubject getSubjectFromBitmap(Bitmap bitmap) {
        NSubject subject = new NSubject();
        NImage nImage = NImage.fromBitmap(bitmap);
        NFace nFace = new NFace();
        nFace.setImage(nImage);
        subject.getFaces().add(nFace);
        NBiometricStatus status = mBiometricClient.createTemplate(subject);
        if (status == NBiometricStatus.OK) {
            return subject;
        }
        return null;
    }
}
