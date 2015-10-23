package co.blustor.gatekeeper.data;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class RemoteFilestore implements AsyncFilestore {
    private Stack<List<File>> mFileTree;
    private boolean mFailTemporarily = false;

    public RemoteFilestore() {
        mFileTree = new Stack<>();
        mFileTree.push(generateFiles());
    }

    @Override
    public void listFiles(final Listener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (mFailTemporarily) {
                    listener.onListFilesError();
                } else {
                    listener.onListFiles(mFileTree.peek());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void navigateTo(String path) {
        if (path.contains("Shortcut")) {
            mFailTemporarily = true;
        } else {
            mFailTemporarily = false;
            mFileTree.push(generateFiles());
        }
    }

    @Override
    public void navigateUp() {
        mFailTemporarily = false;
        if (mFileTree.size() > 1) {
            mFileTree.pop();
        }
    }

    @Override
    public void finish() {
    }

    private List<File> generateFiles() {
        ArrayList<File> items = new ArrayList<>();
        int fileCount = new Random().nextInt(100);
        int fileDirectoryRatio = 4;
        for (int i = 1; i <= fileCount; ) {
            if (new Random().nextInt(fileDirectoryRatio + 1) == 0) {
                items.add(randomDirectory(++i));
            } else {
                items.add(randomFile(++i));
            }
        }
        return items;
    }

    private File randomFile(int val) {
        int nameIndex = new Random().nextInt(fileNames.length);
        String name = String.format(fileNames[nameIndex], val);
        return new MemoryFile(name, File.Type.FILE);
    }

    private File randomDirectory(int val) {
        int nameIndex = new Random().nextInt(directoryNames.length);
        String name = String.format(directoryNames[nameIndex], val);
        return new MemoryFile(name, File.Type.DIRECTORY);
    }

    private String badFile = "NoT_a_ViRuS_%d.exe";
    private String badDirectory = "Shortcut to Copy of My Documents (%d)";

    private String[] fileNames = new String[]{
            "important.%d.txt",
            "kittens (%d).jpg",
            "poetry%d.doc",
            "recipe number %d.pdf",
            badFile
    };

    private String[] directoryNames = new String[]{
            "unorganized_%d",
            "SORTME%d",
            "things %d",
            badDirectory
    };

    private class MemoryFile implements File {
        private String mName;
        private Type mType;

        public MemoryFile(String name, Type type) {
            mName = name;
            mType = type;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public Type getType() {
            return mType;
        }
    }
}
