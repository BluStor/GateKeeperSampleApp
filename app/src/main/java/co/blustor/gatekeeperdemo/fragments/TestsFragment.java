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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.scopes.GKAuthentication;
import co.blustor.gatekeeper.scopes.GKCardSettings;
import co.blustor.gatekeeper.scopes.GKFileActions;
import co.blustor.gatekeeperdemo.R;

public class TestsFragment extends DemoFragment {
    public static final String TAG = TestsFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);
        initializeAuthActions(view);
        initializeCardSettingsActions(view);
        initializeFileActions(view);
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
                addDemoTemplate();
            }
        });
        Button addBadTemplate = (Button) view.findViewById(R.id.add_bad_template);
        addBadTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile("BadTemplate.dat", GKAuthentication.ENROLL_FACE_PATH_PREFIX + "0");
            }
        });
        Button authenticateGoodTemplate = (Button) view.findViewById(R.id.authenticate_good_template);
        authenticateGoodTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateDemoTemplate();
            }
        });
        Button authenticateDifferentTemplate = (Button) view.findViewById(R.id.authenticate_different_template);
        authenticateDifferentTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate("OtherTemplate.dat");
            }
        });
        Button authenticateBadTemplate = (Button) view.findViewById(R.id.authenticate_bad_template);
        authenticateBadTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile("BadTemplate.dat", GKAuthentication.SIGN_IN_PATH);
            }
        });
        Button deleteTemplate = (Button) view.findViewById(R.id.delete_template);
        deleteTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTemplate();
            }
        });
        Button signOut = (Button) view.findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
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

    public void initializeFileActions(View view) {
        Button createDirectoryTests = (Button) view.findViewById(R.id.create_directory_tests);
        createDirectoryTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDirectory("/tests");
            }
        });
        Button createDirectoryNested = (Button) view.findViewById(R.id.create_directory_nested);
        createDirectoryNested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDirectory("/tests/nested");
            }
        });
        Button removeDirectoryTests = (Button) view.findViewById(R.id.remove_directory_tests);
        removeDirectoryTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDirectory("/tests");
            }
        });
        Button removeDirectoryNested = (Button) view.findViewById(R.id.remove_directory_nested);
        removeDirectoryNested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDirectory("/tests/nested");
            }
        });
        Button listDirectoryTests = (Button) view.findViewById(R.id.list_directory_tests);
        listDirectoryTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFiles("/tests");
            }
        });
        Button uploadTestsImage = (Button) view.findViewById(R.id.upload_tests_image);
        uploadTestsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile("image.jpg", "/tests/image.jpg");
            }
        });
        Button downloadTestsImage = (Button) view.findViewById(R.id.download_tests_image);
        downloadTestsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFile("/tests/image.jpg");
            }
        });
        Button deleteTestsImage = (Button) view.findViewById(R.id.delete_tests_image);
        deleteTestsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile("/tests/image.jpg");
            }
        });
        Button removeTestsImageAsDirectory = (Button) view.findViewById(R.id.remove_tests_image_folder);
        removeTestsImageAsDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDirectory("/tests/image.jpg");
            }
        });
        Button deleteDirectoryTestsAsFile = (Button) view.findViewById(R.id.delete_directory_tests_file);
        deleteDirectoryTestsAsFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile("/tests");
            }
        });
    }

    private void listTemplates() {
        new AuthTask() {
            public List<Object> mTemplates;

            @Override
            protected GKAuthentication.Status perform() throws IOException {
                GKAuthentication.ListTemplatesResult result = auth.listTemplates();
                mTemplates = result.getTemplates();
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

    private void authenticate(final String filename) {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return mDemoHelper.authenticateWithFile(filename, mCard, mFaceExtractor).getStatus();
            }
        }.execute();
    }

    private void authenticateDemoTemplate() {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return mDemoHelper.bypassAuthentication(mCard, mFaceExtractor).getStatus();
            }
        }.execute();
    }

    private void deleteTemplate() {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.revokeFace().getStatus();
            }
        }.execute();
    }

    private void signOut() {
        new AuthTask() {
            @Override
            protected GKAuthentication.Status perform() throws IOException {
                return auth.signOut().getStatus();
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

    private void listFiles(final String cardPath) {
        new FilesTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                return mCard.list(cardPath);
            }
        }.execute();
    }

    private void sendFile(final String filename, final String cardPath) {
        new FilesTask() {
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

    private void readFile(final String cardPath) {
        new FilesTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                return mCard.get(cardPath);
            }
        }.execute();
    }

    private void deleteFile(final String cardPath) {
        new FilesTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                return mCard.delete(cardPath);
            }
        }.execute();
    }

    private void createDirectory(final String cardPath) {
        new FilesTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                return mCard.createPath(cardPath);
            }
        }.execute();
    }

    private void removeDirectory(final String cardPath) {
        new FilesTask() {
            @Override
            protected GKCard.Response perform() throws IOException {
                return mCard.deletePath(cardPath);
            }
        }.execute();
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

    private abstract class FilesTask extends AsyncTask<Void, Void, GKCard.Response> {
        protected final GKFileActions fileActions = new GKFileActions(mCard);
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
