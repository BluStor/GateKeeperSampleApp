package co.blustor.gatekeeper.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import co.blustor.gatekeeper.data.File;
import co.blustor.gatekeeper.R;

public class FileBrowserView extends RelativeLayout {
    private GridView mGridView;

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

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_file_browser, this, true);
        mGridView = (GridView) findViewById(R.id.grid);
    }

    public static class IconView extends LinearLayout {
        private ImageView mIconView;
        private TextView mFileNameView;

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
            mIconView.setImageResource(getIconResource(file));
            mFileNameView.setText(file.getName());
        }

        private int getIconResource(File item) {
            if (item.getType() == File.Type.DIRECTORY) {
                return android.R.drawable.ic_menu_my_calendar;
            }
            return android.R.drawable.ic_menu_add;
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
}
