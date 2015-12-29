package co.blustor.gatekeeperdemo.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.neurotec.biometrics.NSubject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeper.scopes.GKCardSettings;
import co.blustor.gatekeeperdemo.R;

public class TestsFragment extends CardFragment {
    public static final String TAG = TestsFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        initializeAuthActions(view);
        initializeCardSettingsActions(view);
        return view;
    }

    public void initializeAuthActions(View view) {
        Button listTemplates = (Button) view.findViewById(R.id.list_templates);
        listTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listTemplates();
            }
        });
        Button addGoodTemplate = (Button) view.findViewById(R.id.add_good_template);
        addGoodTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTemplate("GoodTemplate.dat");
            }
        });
        Button addBadTemplate = (Button) view.findViewById(R.id.add_bad_template);
        addBadTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile("BadTemplate.dat", "/auth/face/0");
            }
        });
        Button authenticateGoodTemplate = (Button) view.findViewById(R.id.authenticate_good_template);
        authenticateGoodTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate("GoodTemplate.dat");
            }
        });
        Button authenticateBadTemplate = (Button) view.findViewById(R.id.authenticate_bad_template);
        authenticateBadTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile("BadTemplate.dat", "/auth/signin/face");
            }
        });
        Button deleteTemplate = (Button) view.findViewById(R.id.delete_template);
        deleteTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTemplate();
            }
        });
    }

    public void initializeCardSettingsActions(View view) {
        Button updateGoodFirmware = (Button) view.findViewById(R.id.update_good_firmware);
        updateGoodFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFirmware("GoodFirmware.dat");
            }
        });
        Button updateBadFirmware = (Button) view.findViewById(R.id.update_bad_firmware);
        updateBadFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFirmware("BadFirmware.dat");
            }
        });
    }

    private void listTemplates() {
        new AuthTask() {
            public List<Object> mTemplates;

            @Override
            protected GKAuthentication.Status perform() throws IOException {
                mTemplates = auth.listTemplates();
                return null;
            }

            @Override
            protected void onPostExecute(GKAuthentication.Status status) {
                super.onPostExecute(status);
                if (mTemplates != null) {
                    reportString(mTemplates.size() + " templates");
                }
            }
        }.execute();
    }

    private void addTemplate(final String filename) {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                String templatePath = getAbsolutePath(filename);
                NSubject subject = NSubject.fromFile(templatePath);
                return auth.enrollWithFace(subject);
            }
        }.execute();
    }

    private void authenticate(final String filename) {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                String templatePath = getAbsolutePath(filename);
                NSubject subject = NSubject.fromFile(templatePath);
                return auth.signInWithFace(subject);
            }
        }.execute();
    }

    private void deleteTemplate() {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.revokeFace();
            }
        }.execute();
    }

    private void sendFirmware(final String filename) {
        new CardSettingsTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                String file = getAbsolutePath(filename);
                return cardSettings.updateFirmware(new FileInputStream(file));
            }
        }.execute();
    }

    @NonNull
    private String getAbsolutePath(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, filename).getAbsolutePath();
    }

    private void sendFile(final String filename, final String cardPath) {
        new CardTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                String file = getAbsolutePath(filename);
                GKCard.Response response = mCard.put(cardPath, new FileInputStream(file));
                if (response.getStatus() != 226) {
                    return response;
                }
                return mCard.finalize(cardPath);
            }
        }.execute();
    }

    private void reportString(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void reportStatus(GKAuthentication.Status status) {
        Toast.makeText(getContext(), status.name(), Toast.LENGTH_LONG).show();
    }

    private void reportResponse(GKCard.Response response) {
        Toast.makeText(getContext(), response.getStatusMessage(), Toast.LENGTH_LONG).show();
    }

    private void reportException(IOException e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private abstract class CardTask extends AsyncTask<Void, Void, GKCard.Response> {
        protected IOException mIOException;

        protected abstract GKCard.Response perform() throws IOException;

        @Override
        protected GKCard.Response doInBackground(Void... params) {
            try {
                return perform();
            } catch (IOException e) {
                mIOException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(GKCard.Response response) {
            super.onPostExecute(response);
            if (response != null) {
                reportResponse(response);
            }
            if (mIOException != null) {
                reportException(mIOException);
            }
        }
    }

    private abstract class AuthTask extends AsyncTask<Void, Void, GKAuthentication.Status> {
        protected final GKAuthentication auth = new GKAuthentication(mCard);
        protected IOException mIOException;

        protected abstract GKAuthentication.Status perform() throws IOException;

        @Override
        protected GKAuthentication.Status doInBackground(Void... params) {
            try {
                return perform();
            } catch (IOException e) {
                mIOException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(GKAuthentication.Status status) {
            super.onPostExecute(status);
            if (status != null) {
                reportStatus(status);
            }
            if (mIOException != null) {
                reportException(mIOException);
            }
        }
    }

    private abstract class CardSettingsTask extends AsyncTask<Void, Void, GKCard.Response> {
        protected final GKCardSettings cardSettings = new GKCardSettings(mCard);
        protected IOException mIOException;

        protected abstract GKCard.Response perform() throws IOException;

        @Override
        protected GKCard.Response doInBackground(Void... params) {
            try {
                return perform();
            } catch (IOException e) {
                mIOException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(GKCard.Response response) {
            super.onPostExecute(response);
            if (response != null) {
                reportResponse(response);
            }
            if (mIOException != null) {
                reportException(mIOException);
            }
        }
    }
}
