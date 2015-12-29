package co.blustor.gatekeeperdemo.filevault;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LocalFilestore {
    public static final String TAG = LocalFilestore.class.getSimpleName();

    File mCachePath;

    public LocalFilestore(File cachePath) {
        mCachePath = cachePath;
    }

    public java.io.File makeTempPath() throws IOException {
        String uniqueDir = UUID.randomUUID().toString();
        File path = new File(mCachePath, uniqueDir);
        path.mkdirs();
        return path;
    }

    public void clearCache() {
        File[] cacheItems = mCachePath.listFiles();
        for (File cacheItem : cacheItems) {
            deleteCacheItem(cacheItem);
        }
    }

    private void deleteCacheItem(File path) {
        if (path.isDirectory()) {
            File[] cacheItems = path.listFiles();
            for (File cacheItem : cacheItems) {
                deleteCacheItem(cacheItem);
            }
        }
        path.delete();
    }
}
