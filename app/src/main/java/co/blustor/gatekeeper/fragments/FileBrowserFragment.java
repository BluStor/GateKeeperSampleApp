package co.blustor.gatekeeper.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.AsyncFilestore;
import co.blustor.gatekeeper.data.File;
import co.blustor.gatekeeper.data.RemoteFilestore;
import co.blustor.gatekeeper.ui.FileBrowserView;

public class FileBrowserFragment extends Fragment implements AsyncFilestore.Listener, FileBrowserView.BrowseListener {
    private FileBrowserView mFileGrid;
    private AsyncFilestore mFilestore;

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
        mFilestore = new RemoteFilestore();
        mFilestore.listFiles(this);
    }

    private void uninitializeClient() {
        mFilestore.finish();
    }

    @Override
    public void onListFiles(final List<File> files) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileGrid.setAdapter(new FileBrowserView.Adapter(getActivity(), files));
            }
        });
    }

    @Override
    public void onListFilesError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "Unable to show files", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDirectoryClick(File file) {
        mFilestore.navigateTo(file.getName());
        mFilestore.listFiles(this);
    }

    @Override
    public void onFileClick(File file) {
        Toast.makeText(getActivity(), "file '" + file.getName() + "' clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateBack() {
        mFilestore.navigateUp();
        mFilestore.listFiles(this);
    }
}
