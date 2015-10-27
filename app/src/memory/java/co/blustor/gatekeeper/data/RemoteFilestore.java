package co.blustor.gatekeeper.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import co.blustor.gatekeeper.Application;
import co.blustor.gatekeeper.util.FileUtils;
import co.blustor.gatekeeper.util.StringUtils;

public class RemoteFilestore implements AsyncFilestore {
    public final static String TAG = RemoteFilestore.class.getSimpleName();

    private Stack<String> mCurrentPath = new Stack<>();

    @Override
    public void listFiles(final Listener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listener.onListFiles(listAssetFiles(getCurrentPath()));
                return null;
            }
        }.execute();
    }

    @Override
    public void getFile(final AbstractFile file, final File targetPath, final Listener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    File downloaded = copyAssetFile(file, targetPath);
                    listener.onGetFile(new DroidFilestore.CachedFile(downloaded));
                } catch (IOException e) {
                    Log.e(TAG, "failed to get file", e);
                    listener.onGetFileError(e);
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void navigateTo(String path) {
        mCurrentPath.push(path);
    }

    @Override
    public void navigateUp() {
        if (!mCurrentPath.empty()) {
            mCurrentPath.pop();
        }
    }

    @Override
    public void finish() {
    }

    private String getCurrentPath() {
        if (mCurrentPath.empty()) {
            return "ftp";
        }
        return "ftp/" + StringUtils.join(mCurrentPath.toArray(), "/");
    }

    private List<AbstractFile> listAssetFiles(String targetPath) {
        ArrayList<AbstractFile> files = new ArrayList<>();
        Context context = Application.getAppContext();
        try {
            String[] filenames = context.getAssets().list(targetPath);
            for (String name : filenames) {
                if (name.contains(".")) {
                    files.add(new AbstractFile(name, AbstractFile.Type.FILE));
                } else {
                    files.add(new AbstractFile(name, AbstractFile.Type.DIRECTORY));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "problem getting ftp files", e);
        }
        return files;
    }

    private File copyAssetFile(AbstractFile file, File targetPath) throws IOException {
        Context context = Application.getAppContext();
        String filePath = getCurrentPath() + "/" + file.getName();
        InputStream inputStream = context.getAssets().open(filePath);
        File targetFile = new File(targetPath, file.getName());
        FileUtils.writeStreamToFile(inputStream, targetFile);
        return targetFile;
    }
}
