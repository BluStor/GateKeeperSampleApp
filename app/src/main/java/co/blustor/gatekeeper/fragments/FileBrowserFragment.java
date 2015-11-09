package co.blustor.gatekeeper.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeper.Configuration;
import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.VaultFile;
import co.blustor.gatekeeper.dialogs.FileProgressDialogFragment;
import co.blustor.gatekeeper.ui.FileBrowserView;

public class FileBrowserFragment
        extends Fragment
        implements
        FileVault.ListFilesListener,
        FileVault.GetFileListener,
        FileVault.PutFileListener,
        FileVault.DeleteFileListener,
        FileVault.MakeDirectoryListener,
        FileBrowserView.BrowseListener {
    public static final String TAG = FileBrowserFragment.class.getSimpleName();

    public static final int VIEW_FILE_REQUEST = 1;
    public static final int CHOOSE_FILE_REQUEST = 2;

    private FileBrowserView mFileGrid;
    private FileProgressDialogFragment mFileProgressDialogFragment;
    private FileVault mFileVault;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);
        initializeViews(view);
        initalizeFragments();
        initializeData();
        return view;
    }

    @Override
    public void onDestroyView() {
        uninitializeClient();
        super.onDestroyView();
    }

    private void initializeViews(View view) {
        mFileGrid = (FileBrowserView) view.findViewById(R.id.file_browser);
        mFileGrid.setBrowseListener(this);
    }

    private void initalizeFragments() {
        mFileProgressDialogFragment = new FileProgressDialogFragment();
    }

    private void initializeData() {
        mFileVault = Configuration.getFileVault();
        mFileVault.listFiles(this);
    }

    private void uninitializeClient() {
        mFileVault.finish();
    }

    @Override
    public void onListFiles(final List<VaultFile> files) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileGrid.setAdapter(new FileBrowserView.Adapter(getActivity(), files));
            }
        });
    }

    @Override
    public void onListFilesError(final IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Unable to list files", e);
                Toast.makeText(getActivity(), "Unable to show files", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onGetFile(final VaultFile cachedFile) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileProgressDialogFragment.dismiss();
                viewFile(cachedFile);
            }
        });
    }

    @Override
    public void onGetFileError(final IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileProgressDialogFragment.dismiss();
                Log.e(TAG, "Unable to get file", e);
                Toast.makeText(getActivity(), "Unable to get file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPutFile() {
        mFileVault.listFiles(this);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileProgressDialogFragment.dismiss();
                Toast toast = Toast.makeText(getActivity(), "File Uploaded", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onPutFileError(IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileProgressDialogFragment.dismiss();
                Toast toast = Toast.makeText(getActivity(), "File Upload Failed", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onDeleteFile(final VaultFile file) {
        mFileVault.listFiles(this);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "";
                if (file.getType() == VaultFile.Type.FILE) {
                    text = "File Deleted";
                } else {
                    text = "Directory Deleted";
                }
                Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onDeleteFileError(final VaultFile file, IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "";
                if (file.getType() == VaultFile.Type.FILE) {
                    text = "Failed to delete file";
                } else {
                    text = "Failed to delete directory";
                }
                Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onMakeDirectory() {
        mFileVault.listFiles(this);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getActivity(), "Directory created.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onMakeDirectoryError(IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getActivity(), "Failed to create directory.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onDirectoryClick(VaultFile file) {
        mFileVault.listFiles(file, this);
    }

    @Override
    public void onDirectoryLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_directory_confirmation);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileBrowserFragment.this);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    public void onFileClick(VaultFile file) {
        mFileProgressDialogFragment.setText(R.string.file_download_in_progress_text);
        mFileProgressDialogFragment.show(
                getActivity().getFragmentManager(),
                mFileProgressDialogFragment.getClass().getSimpleName());
        mFileVault.getFile(file, this);
    }

    public boolean canNavigateBack() {
        return !mFileVault.isAtRoot();
    }

    @Override
    public void onFileLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_file_confirmation);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileBrowserFragment.this);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    public void navigateBack() {
        mFileVault.navigateUp();
        mFileVault.listFiles(this);
    }

    @Override
    public void onUploadButtonClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Please select a file using..."), CHOOSE_FILE_REQUEST);
    }

    @Override
    public void onCreateDirectoryButtonClick() {
        Context context = getActivity();
        final EditText editText = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.name_directory_prompt);
        builder.setView(editText);
        builder.setPositiveButton(R.string.name_directory_ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String directoryName = String.valueOf(editText.getText()).trim();
                Log.e(TAG, "The Directory Name is... " + directoryName);
                mFileVault.makeDirectory(directoryName, FileBrowserFragment.this);
            }
        });
        builder.setNegativeButton(R.string.name_directory_cancel_button, null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VIEW_FILE_REQUEST:
                mFileVault.clearCache();
                Log.i(TAG, "Finished Viewing File");
                break;

            case CHOOSE_FILE_REQUEST:
                if (data == null)
                    return;
                Uri uri = data.getData();
                try {
                    InputStream is = getInputStream(uri);
                    String filename = getFileName(uri);
                    mFileProgressDialogFragment.setText(R.string.file_upload_in_progress_text);
                    mFileProgressDialogFragment.show(
                            getActivity().getFragmentManager(),
                            mFileProgressDialogFragment.getClass().getSimpleName());
                    mFileVault.putFile(is, filename, this);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found.");
                    e.printStackTrace();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private InputStream getInputStream(Uri fileUri) throws FileNotFoundException {
        ContentResolver resolver = getActivity().getContentResolver();
        return resolver.openInputStream(fileUri);
    }

    private String getFileName(Uri fileUri) {
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(fileUri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        return cursor.getString(nameIndex);
    }

    private void viewFile(VaultFile cachedFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(cachedFile.getLocalPath());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(cachedFile.getExtension());
        intent.setDataAndType(uri, mimeType);
        try {
            startActivityForResult(intent, VIEW_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "no handler", e);
            Toast.makeText(getActivity(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }
}
