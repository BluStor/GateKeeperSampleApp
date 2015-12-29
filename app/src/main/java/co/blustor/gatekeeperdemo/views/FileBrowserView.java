package co.blustor.gatekeeperdemo.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import co.blustor.gatekeeperdemo.R;
import co.blustor.gatekeeperdemo.filevault.VaultFile;

public class FileBrowserView extends RelativeLayout {
    private Button mBackButton;
    private Button mCreateDirectoryButton;
    private Button mUploadButton;
    private RecyclerView mGridView;
    private BrowseListener mBrowseListener;

    private boolean mBackEnabled;
    private int mColumnWidth = 192;

    public FileBrowserView(Context context) {
        super(context);
        init(null);
    }

    public FileBrowserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FileBrowserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mGridView.setAdapter(adapter);
    }

    public void setBrowseListener(BrowseListener listener) {
        mBrowseListener = listener;
    }

    public void disableButtons() {
        mBackButton.setEnabled(false);
        mCreateDirectoryButton.setEnabled(false);
        mUploadButton.setEnabled(false);
    }

    public void enableButtons() {
        mBackButton.setEnabled(mBackEnabled);
        mCreateDirectoryButton.setEnabled(true);
        mUploadButton.setEnabled(true);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_file_browser, this, true);
        mBackButton = (Button) findViewById(R.id.previous_directory);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowseListener.navigateBack();
            }
        });
        mCreateDirectoryButton = (Button) findViewById(R.id.create_directory_button);
        mCreateDirectoryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowseListener.onCreateDirectoryButtonClick();
            }
        });
        mUploadButton = (Button) findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowseListener.onUploadButtonClick();
            }
        });
        mGridView = (RecyclerView) findViewById(R.id.grid);
        if (attrs != null) {
            int[] attrsArray = {android.R.attr.columnWidth};
            TypedArray array = getContext().obtainStyledAttributes(attrs, attrsArray);
            mColumnWidth = array.getDimensionPixelSize(0, 123);
            array.recycle();
        }
        LinearLayoutManager layoutManager = new GridAutofitLayoutManager(getContext(), mColumnWidth);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mGridView.setLayoutManager(layoutManager);
    }

    public void setBackEnabled(boolean enabled) {
        mBackEnabled = enabled;
        mBackButton.setEnabled(mBackEnabled);
    }

    public class GridAutofitLayoutManager extends GridLayoutManager {
        private int mColumnWidth;
        private boolean mColumnWidthChanged = true;

        public GridAutofitLayoutManager(Context context, int columnWidth) {
            super(context, 1);
            setColumnWidth(checkedColumnWidth(context, columnWidth));
        }

        private int checkedColumnWidth(Context context, int columnWidth) {
            if (columnWidth <= 0) {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, displayMetrics);
            }
            return columnWidth;
        }

        public void setColumnWidth(int newColumnWidth) {
            if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
                mColumnWidth = newColumnWidth;
                mColumnWidthChanged = true;
            }
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            if (mColumnWidthChanged && mColumnWidth > 0) {
                int totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
                int spanCount = Math.max(1, totalSpace / mColumnWidth);
                setSpanCount(spanCount);
                mColumnWidthChanged = false;
            }
            super.onLayoutChildren(recycler, state);
        }
    }

    public interface BrowseListener {
        void onDirectoryClick(VaultFile file);
        void onDirectoryLongClick(VaultFile file);
        void onFileClick(VaultFile file);
        void onFileLongClick(VaultFile file);
        void navigateBack();
        void onUploadButtonClick();
        void onCreateDirectoryButtonClick();
    }
}
