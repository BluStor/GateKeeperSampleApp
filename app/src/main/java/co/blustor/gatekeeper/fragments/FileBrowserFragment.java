package co.blustor.gatekeeper.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.Configuration;
import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.FileVault;
import co.blustor.gatekeeper.data.VaultFile;
import co.blustor.gatekeeper.ui.FileBrowserView;

public class FileBrowserFragment extends Fragment implements FileVault.ListFilesListener, FileVault.GetFileListener, FileBrowserView.BrowseListener {
    public static final String TAG = FileBrowserFragment.class.getSimpleName();

    public static final int VIEW_FILE_REQUEST = 1;

    private FileBrowserView mFileGrid;
    private FileVault mFileVault;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);
        initializeViews(view);
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
                viewFile(cachedFile);
            }
        });
    }

    @Override
    public void onGetFileError(final IOException e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Unable to get file", e);
                Toast.makeText(getActivity(), "Unable to get file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDirectoryClick(VaultFile file) {
        mFileVault.listFiles(file, this);
    }

    @Override
    public void onFileClick(VaultFile file) {
        mFileVault.getFile(file, this);
    }

    @Override
    public void navigateBack() {
        mFileVault.navigateUp();
        mFileVault.listFiles(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != VIEW_FILE_REQUEST) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.i(TAG, "Finished Viewing File");
        }
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
