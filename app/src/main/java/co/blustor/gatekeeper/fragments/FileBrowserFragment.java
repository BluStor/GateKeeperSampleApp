package co.blustor.gatekeeper.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
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
import co.blustor.gatekeeper.apps.FileVault;
import co.blustor.gatekeeper.data.VaultFile;
import co.blustor.gatekeeper.views.FileBrowserView;

public class FileBrowserFragment extends Fragment implements FileVault.ListFilesListener,
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
        mFileVault = Configuration.getFileVault();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);
        initializeViews(view);
        initializeData();
        return view;
    }

    @Override
    public void onDestroyView() {
        mFileVault.finish();
        super.onDestroyView();
    }

    private void initializeViews(View view) {
        mFileGrid = (FileBrowserView) view.findViewById(R.id.file_browser);
        mFileGrid.setBrowseListener(this);
        mFileProgressDialogFragment = new FileProgressDialogFragment();
    }

    private void initializeData() {
        if (mFileVault.remoteAvailable()) {
            mFileVault.listFiles(this);
        } else {
            mFileGrid.disableButtons();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.remote_filestore_unavailable);
            builder.setPositiveButton(R.string.okay, null);
            builder.show();
        }
    }

    @Override
    public void onListFiles(final List<VaultFile> files) {
        mFileGrid.setBackEnabled(!mFileVault.isAtRoot());
        mFileGrid.setAdapter(new FileBrowserView.Adapter(getActivity(), files));
    }

    @Override
    public void onListFilesError(final IOException e) {
        Log.e(TAG, "Unable to List Files", e);
        showShortMessage(R.string.file_list_failure);
    }

    @Override
    public void onGetFile(final VaultFile cachedFile) {
        mFileProgressDialogFragment.dismiss();
        viewFile(cachedFile);
    }

    @Override
    public void onGetFileError(final IOException e) {
        Log.e(TAG, "Unable to Get File", e);
        mFileProgressDialogFragment.dismiss();
        showShortMessage(R.string.file_load_failure);
    }

    @Override
    public void onPutFile() {
        mFileVault.listFiles(this);
        mFileProgressDialogFragment.dismiss();
        showLongMessage(R.string.file_upload_success);
    }

    @Override
    public void onPutFileError(IOException e) {
        mFileProgressDialogFragment.dismiss();
        showLongMessage(R.string.file_upload_failure);
    }

    @Override
    public void onDeleteFile(final VaultFile file) {
        mFileVault.listFiles(this);
        boolean isFile = file.getType() == VaultFile.Type.FILE;
        showLongMessage(isFile ? R.string.file_delete_success : R.string.folder_delete_success);
    }

    @Override
    public void onDeleteFileError(final VaultFile file, IOException e) {
        boolean isFile = file.getType() == VaultFile.Type.FILE;
        showLongMessage(isFile ? R.string.file_delete_failure : R.string.folder_delete_failure);
    }

    @Override
    public void onMakeDirectory() {
        mFileVault.listFiles(this);
        showLongMessage(R.string.folder_create_success);
    }

    @Override
    public void onMakeDirectoryError(IOException e) {
        showLongMessage(R.string.folder_create_failure);
    }

    @Override
    public void onDirectoryClick(VaultFile file) {
        mFileVault.listFiles(file, this);
    }

    @Override
    public void onDirectoryLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_directory_confirmation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileBrowserFragment.this);
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    @Override
    public void onFileClick(VaultFile file) {
        mFileProgressDialogFragment.setText(R.string.file_download_in_progress_text);
        mFileProgressDialogFragment.show(getActivity().getSupportFragmentManager(), FileProgressDialogFragment.TAG);
        mFileVault.getFile(file, this);
    }

    public boolean canNavigateBack() {
        return mFileVault.remoteAvailable() && !mFileVault.isAtRoot();
    }

    @Override
    public void onFileLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_file_confirmation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileBrowserFragment.this);
            }
        });
        builder.setNegativeButton(R.string.no, null);
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
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), CHOOSE_FILE_REQUEST);
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
                clearVaultCache();
                break;
            case CHOOSE_FILE_REQUEST:
                uploadSelectedFile(data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showShortMessage(final int resource) {
        Toast.makeText(getActivity(), resource, Toast.LENGTH_LONG).show();
    }

    private void showLongMessage(final int resource) {
        Toast.makeText(getActivity(), resource, Toast.LENGTH_LONG).show();
    }

    private void clearVaultCache() {
        mFileVault.clearCache();
        Log.i(TAG, "Finished Viewing File");
    }

    private void uploadSelectedFile(Intent data) {
        if (data == null) { return; }
        Uri uri = data.getData();
        try {
            InputStream is = getInputStream(uri);
            String filename = getFileName(uri);
            mFileProgressDialogFragment.setText(R.string.file_upload_in_progress_text);
            mFileProgressDialogFragment.show(getActivity().getSupportFragmentManager(), FileProgressDialogFragment.TAG);
            mFileVault.putFile(is, filename, this);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File Not Found", e);
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
        String extension = cachedFile.getExtension();
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        intent.setDataAndType(uri, mimeType);
        try {
            startActivityForResult(intent, VIEW_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No Handler for File Type", e);
            showLongMessage(R.string.file_handler_not_found);
        }
    }
}
