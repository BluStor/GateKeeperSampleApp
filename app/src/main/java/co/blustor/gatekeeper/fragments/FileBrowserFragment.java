package co.blustor.gatekeeper.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import co.blustor.gatekeeper.R;
import co.blustor.gatekeeper.data.File;
import co.blustor.gatekeeper.ui.FileBrowserView;

import static co.blustor.gatekeeper.data.File.Type.FILE;

public class FileBrowserFragment extends Fragment {
    private FileBrowserView mFileGrid;

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

    private void initializeData() {
        ArrayList<File> items = new ArrayList<File>();
        for (int i = 1; i <= 100; ) {
            items.add(new FileBrowserView.BrowserFile("myFile " + ++i + ".txt", FILE));
            items.add(new FileBrowserView.BrowserFile("patch" + ++i + ".txt", FILE));
            items.add(new FileBrowserView.BrowserFile("cats" + ++i + ".jpg", FILE));
            items.add(new FileBrowserView.BrowserFile("stuff" + ++i, File.Type.DIRECTORY));
        }
        mFileGrid.setAdapter(new FileBrowserView.Adapter(this.getActivity(), items));
    }

    private void initializeViews(View view) {
        mFileGrid = (FileBrowserView) view.findViewById(R.id.file_browser);
    }
}
