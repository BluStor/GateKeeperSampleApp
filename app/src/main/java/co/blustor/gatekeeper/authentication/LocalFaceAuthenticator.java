package co.blustor.gatekeeper.authentication;

import android.util.Log;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;

import java.io.IOException;
import java.util.EnumSet;

import co.blustor.gatekeeper.data.Datastore;

public class LocalFaceAuthenticator {
    public static final String TAG = LocalFaceAuthenticator.class.getSimpleName();

    private Datastore mDataStore;

    public LocalFaceAuthenticator(Datastore datastore) {
        mDataStore = datastore;
    }

    public boolean authenticate(NSubject testSubject) {
        NSubject enrolledSubject = null;
        try {
            enrolledSubject = mDataStore.getTemplate();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get enrolled template from Datastore.");
            e.printStackTrace();
            return false;
        }

        NBiometricClient client = new NBiometricClient();

        NBiometricTask enrollTask = client.createTask(EnumSet.of(NBiometricOperation.ENROLL), enrolledSubject);
        NBiometricTask identifyTask = client.createTask(EnumSet.of(NBiometricOperation.IDENTIFY), testSubject);

        client.performTask(enrollTask);
        client.performTask(identifyTask);

        return identifyTask.getStatus() == NBiometricStatus.OK;
    }
}
