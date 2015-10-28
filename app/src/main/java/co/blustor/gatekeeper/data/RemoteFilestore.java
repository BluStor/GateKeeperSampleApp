package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import co.blustor.gatekeeper.util.FileUtils;

public class RemoteFilestore {
    public static String TAG = RemoteFilestore.class.getSimpleName();

    private final RemoteFilestoreClient mFilestoreClient;
    private Stack<String> mCurrentPath = new Stack<>();

    public RemoteFilestore(RemoteFilestoreClient client) {
        mFilestoreClient = client;
    }

    public List<VaultFile> listFiles() throws IOException {
        mFilestoreClient.open();
        return mFilestoreClient.listFiles(getCurrentPath());
    }

    public File getFile(final VaultFile file) throws IOException {
        return mFilestoreClient.downloadFile(file);
    }

    public void navigateTo(String path) {
        mCurrentPath.push(path);
    }

    public void navigateUp() {
        if (!mCurrentPath.empty()) {
            mCurrentPath.pop();
        }
    }

    public void finish() {
        try {
            mFilestoreClient.close();
        } catch (IOException e) {
        }
    }

    private String getCurrentPath() {
        String rootPath = mFilestoreClient.getRootPath();
        String subPath = FileUtils.joinPath(mCurrentPath.toArray());
        return FileUtils.joinPath(rootPath, subPath);
    }
}
