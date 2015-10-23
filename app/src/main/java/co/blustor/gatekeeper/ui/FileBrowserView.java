package co.blustor.gatekeeper.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import co.blustor.gatekeeper.data.File;
import co.blustor.gatekeeper.R;

public class FileBrowserView extends RelativeLayout {
    private Button mBackButton;
    private GridView mGridView;
    private BrowseListener mBrowseListener;

    public FileBrowserView(Context context) {
        super(context);
        init();
    }

    public FileBrowserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FileBrowserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setAdapter(Adapter adapter) {
        mGridView.setAdapter(adapter);
    }

    public void setBrowseListener(BrowseListener listener) {
        mBrowseListener = listener;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_file_browser, this, true);
        mBackButton = (Button) findViewById(R.id.previous_directory);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowseListener.navigateBack();
            }
        });
        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBrowseListener == null) return;
                File file = ((IconView) view).getFile();
                if (file.getType() == File.Type.DIRECTORY) {
                    mBrowseListener.onDirectoryClick(file);
                } else {
                    mBrowseListener.onFileClick(file);
                }
            }
        });
    }

    public static class IconView extends LinearLayout {
        private ImageView mIconView;
        private TextView mFileNameView;
        private File mFile;

        public IconView(Context context) {
            super(context);
            init();
        }

        public IconView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            LayoutInflater.from(getContext()).inflate(R.layout.view_file_browser_icon, this, true);
            mIconView = (ImageView) findViewById(R.id.icon);
            mFileNameView = (TextView) findViewById(R.id.name);
        }

        public void setFile(File file) {
            mFile = file;
            mIconView.setImageResource(getIconResource(file));
            mFileNameView.setText(file.getName());
        }

        public File getFile() {
            return mFile;
        }

        private int getIconResource(File item) {
            if (item.getType() == File.Type.DIRECTORY) {
                return R.drawable.ic_folder;
            }
            return R.drawable.ic_file;
        }
    }

    public static class Adapter extends ArrayAdapter<File> {
        public Adapter(Context context, List<File> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IconView itemView = (IconView) convertView;

            if (itemView == null) {
                itemView = new IconView(parent.getContext());
            }

            itemView.setFile(getItem(position));
            return itemView;
        }
    }

    public interface BrowseListener {
        void onDirectoryClick(File file);

        void onFileClick(File file);

        void navigateBack();
    }
}
