package co.blustor.gatekeeperdemo.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.dialogs.FileProgressDialogFragment;
import co.blustor.gatekeepersdk.services.GKAuthentication;
import co.blustor.gatekeepersdk.services.GKCardSettings;

public class SettingsFragment extends CardFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    private static final int LAUNCH_FACE_CAPTURE = 1;
    private static final int FIRMWARE_FILE_PICKER = 2;

    private Button mUpdateFirmware;
    private Button mUpdateFaceTemplate;
    private Button mDeleteFaceTemplate;

    private FileProgressDialogFragment mFileProgressDialogFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mUpdateFirmware = (Button) view.findViewById(R.id.update_firmware);
        mUpdateFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFirmwareFilePicker();
            }
        });
        mUpdateFaceTemplate = (Button) view.findViewById(R.id.update_face_template);
        mUpdateFaceTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFaceCapture();
            }
        });
        mDeleteFaceTemplate = (Button) view.findViewById(R.id.delete_face_template);
        mDeleteFaceTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDeleteFaceTemplate();
            }
        });
        mFileProgressDialogFragment = new FileProgressDialogFragment();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FIRMWARE_FILE_PICKER:
                onFirmwareFilePickerReturn(resultCode, data);
                break;
            case LAUNCH_FACE_CAPTURE:
                onFaceCaptureReturn(resultCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.settings).setEnabled(false);
        super.onPrepareOptionsMenu(menu);
    }

    private void onFirmwareFilePickerReturn(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            new AsyncTask<Void, Void, GKCardSettings.CardResult>() {
                IOException mException;

                @Override
                protected void onPreExecute() {
                    mFileProgressDialogFragment.setFilePutText();
                    mFileProgressDialogFragment.show(getActivity().getSupportFragmentManager(), FileProgressDialogFragment.TAG);
                }

                @Override
                protected GKCardSettings.CardResult doInBackground(Void... params) {
                    Uri uri = data.getData();
                    try {
                        InputStream inputStream = getInputStream(uri);
                        GKCardSettings cardSettings = new GKCardSettings(mCard);
                        return cardSettings.updateFirmware(inputStream);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File Not Found", e);
                    } catch (IOException e) {
                        mException = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(GKCardSettings.CardResult result) {
                    mFileProgressDialogFragment.dismiss();
                    if (mException == null) {
                        showMessage(result.getStatus().toString());
                    } else {
                        showMessage(mException.getMessage());
                    }
                }
            }.execute();
        }
    }

    private InputStream getInputStream(Uri uri) throws FileNotFoundException {
        ContentResolver resolver = getActivity().getContentResolver();
        return resolver.openInputStream(uri);
    }

    private void onFaceCaptureReturn(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
        }
    }

    private void showFirmwareFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file_prompt_message)), FIRMWARE_FILE_PICKER);
    }

    private void launchFaceCapture() {
        getCardActivity().updateTemplate();
    }

    private void promptDeleteFaceTemplate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_template_confirm);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFaceTemplate();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private void deleteFaceTemplate() {
        new SettingsTask() {
            protected GKAuthentication mAuth = new GKAuthentication(mCard);

            @Override
            protected String performCardAction() throws IOException {
                mAuth.revokeFace();
                return getString(R.string.delete_template_success);
            }
        }.execute();
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private abstract class SettingsTask extends AsyncTask<Void, Void, String> {
        protected abstract String performCardAction() throws IOException;

        @Override
        protected String doInBackground(Void... params) {
            try {
                mCard.connect();
                return performCardAction();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String message) {
            showMessage(message);
        }
    }
}
