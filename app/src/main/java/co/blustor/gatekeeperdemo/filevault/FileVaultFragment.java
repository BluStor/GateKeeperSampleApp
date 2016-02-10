package co.blustor.gatekeeperdemo.filevault;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.blustor.gatekeeperdemo.Application;
import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.dialogs.FileProgressDialogFragment;
import co.blustor.gatekeeperdemo.fragments.CardFragment;
import co.blustor.gatekeeperdemo.views.FileBrowserView;

public class FileVaultFragment extends CardFragment implements FileVault.ListFilesListener,
        FileVault.GetFileListener,
        FileVault.PutFileListener,
        FileVault.DeleteFileListener,
        FileVault.MakeDirectoryListener,
        FileBrowserView.BrowseListener {
    public static final String TAG = FileVaultFragment.class.getSimpleName();

    public static final int VIEW_FILE_REQUEST = 1;
    public static final int CHOOSE_FILE_REQUEST = 2;

    private FileBrowserView mFileGrid;
    private FileProgressDialogFragment mFileProgressDialogFragment;
    private Drawable mFileDrawable;
    private Drawable mFolderDrawable;

    private FileVault mFileVault;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalFilestore filestore = Application.getLocalFilestore();
        mFileVault = new FileVault(filestore, mCard);
        mFileVault.setPath("/");
        mFileDrawable = getResources().getDrawable(R.drawable.ic_file);
        mFolderDrawable = getResources().getDrawable(R.drawable.ic_folder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_vault, container, false);
        initializeViews(view);
        initializeData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeViews(View view) {
        mFileGrid = (FileBrowserView) view.findViewById(R.id.file_browser);
        mFileGrid.setBrowseListener(this);
        mFileProgressDialogFragment = new FileProgressDialogFragment();
    }

    private void initializeData() {
        if (mFileVault.cardAvailable()) {
            mFileGrid.enableButtons();
            mFileVault.listFiles(this);
        } else {
            mFileGrid.disableButtons();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.gkcard_reconnect_prompt_title);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    @Override
    public void onListFiles(final List<VaultFile> files) {
        mFileGrid.setBackEnabled(!mFileVault.isAtRoot());
        mFileGrid.setAdapter(new Adapter(files));
    }

    @Override
    public void onListFilesError(final IOException e) {
        Log.e(TAG, "Unable to List Files", e);
        showShortMessage(R.string.file_list_failure_message);
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
        showShortMessage(R.string.file_get_failure_message);
    }

    @Override
    public void onPutFile() {
        mFileVault.listFiles(this);
        mFileProgressDialogFragment.dismiss();
        showLongMessage(R.string.file_put_success_message);
    }

    @Override
    public void onPutFileError(IOException e) {
        mFileProgressDialogFragment.dismiss();
        showLongMessage(R.string.file_put_failure_message);
    }

    @Override
    public void onDeleteFile(final VaultFile file) {
        mFileVault.listFiles(this);
        boolean isFile = file.getType() == VaultFile.Type.FILE;
        showLongMessage(isFile ? R.string.file_delete_success_message : R.string.folder_delete_success_message);
    }

    @Override
    public void onDeleteFileError(final VaultFile file, IOException e) {
        boolean isFile = file.getType() == VaultFile.Type.FILE;
        showLongMessage(isFile ? R.string.file_delete_failure_message : R.string.folder_delete_failure_message);
    }

    @Override
    public void onMakeDirectory() {
        mFileVault.listFiles(this);
        showLongMessage(R.string.folder_create_success_message);
    }

    @Override
    public void onMakeDirectoryError(IOException e) {
        showLongMessage(R.string.folder_create_failure_message);
    }

    @Override
    public void onDirectoryClick(VaultFile file) {
        mFileVault.listFiles(file, this);
    }

    @Override
    public void onDirectoryLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.folder_delete_prompt_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileVaultFragment.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    @Override
    public void onFileClick(VaultFile file) {
        mFileProgressDialogFragment.setFileGetText();
        mFileProgressDialogFragment.show(getActivity().getSupportFragmentManager(), FileProgressDialogFragment.TAG);
        mFileVault.getFile(file, this);
    }

    public boolean canNavigateBack() {
        return mFileVault.cardAvailable() && !mFileVault.isAtRoot();
    }

    @Override
    public void onFileLongClick(final VaultFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.file_delete_prompt_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFileVault.deleteFile(file, FileVaultFragment.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
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
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file_prompt_message)), CHOOSE_FILE_REQUEST);
    }

    @Override
    public void onCreateDirectoryButtonClick() {
        Context context = getActivity();
        final EditText editText = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.folder_create_prompt_message);
        builder.setView(editText);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String directoryName = String.valueOf(editText.getText()).trim();
                mFileVault.makeDirectory(directoryName, FileVaultFragment.this);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
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
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            InputStream is = getInputStream(uri);
            String filename = getFileName(uri);
            mFileProgressDialogFragment.setFilePutText();
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
            showLongMessage(R.string.file_get_failure_no_handler);
        }
    }

    private class Adapter extends RecyclerView.Adapter<FileHolder> {
        private final List<VaultFile> mFiles;

        public Adapter(List<VaultFile> files) {
            mFiles = files;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.view_file_browser_icon, parent, false);
            return new FileHolder(view);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            final VaultFile file = mFiles.get(position);
            final boolean isDirectory = file.getType() == VaultFile.Type.DIRECTORY;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDirectory) {
                        onDirectoryClick(file);
                    } else {
                        onFileClick(file);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (isDirectory) {
                        onDirectoryLongClick(file);
                    } else {
                        onFileLongClick(file);
                    }
                    return true;
                }
            });
            holder.setFile(file);
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        private final ImageView mIconView;
        private final TextView mNameView;

        public FileHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.icon);
            mNameView = (TextView) view.findViewById(R.id.name);
        }

        public void setFile(VaultFile file) {
            boolean isDirectory = file.getType() == VaultFile.Type.DIRECTORY;
            mIconView.setImageDrawable(getIcon(isDirectory));
            mNameView.setText(file.getName());
        }
    }

    private Drawable getIcon(boolean isDirectory) {
        return isDirectory ? mFolderDrawable : mFileDrawable;
    }
}
