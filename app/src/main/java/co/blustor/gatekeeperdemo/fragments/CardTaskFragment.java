package co.blustor.gatekeeperdemo.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeepersdk.biometrics.GKEnvironment;
import co.blustor.gatekeepersdk.biometrics.GKFaces;

public class CardTaskFragment extends Fragment {
    public static final String TAG = CardTaskFragment.class.getSimpleName();
    protected final Object mSyncObject = new Object();
    protected GKFaces mFaces;
    private Callbacks mCallbacks;
    private boolean mLicensesReady;
    private boolean mInitializing;
    private GKEnvironment.InitializationListener mEnvInitListener = new GKEnvironment.InitializationListener() {
        @Override
        public void onLicensesObtained() {
            synchronized (mSyncObject) {
                mLicensesReady = true;
                mInitializing = false;
            }
            preloadFaces();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) getActivity();
        if (mFaces != null) {
            mCallbacks.onFacesReady(mFaces);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    public void initialize() {
        synchronized (mSyncObject) {
            if (!mLicensesReady && !mInitializing) {
                mInitializing = true;
                GKEnvironment.getInstance(getContext()).initialize(mEnvInitListener);
            } else {
                mCallbacks.onFacesReady(mFaces);
            }
        }
    }

    private void preloadFaces() {
        new AsyncTask<Void, Void, GKFaces>() {
            @Override
            protected GKFaces doInBackground(Void... params) {
                return Application.getGKFaces();
            }

            @Override
            protected void onPostExecute(GKFaces faces) {
                synchronized (mSyncObject) {
                    mFaces = faces;
                }
                if (mCallbacks != null) {
                    mCallbacks.onFacesReady(mFaces);
                }
            }
        }.execute();
    }

    public interface Callbacks {
        void onFacesReady(GKFaces faces);
    }
}
