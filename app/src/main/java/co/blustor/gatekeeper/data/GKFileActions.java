package co.blustor.gatekeeper.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.bftp.CardClient;
import co.blustor.gatekeeper.devices.GKCard;

public class GKFileActions {
    public static final String TAG = GKFileActions.class.getSimpleName();

    private final GKCard mCard;

    public GKFileActions(GKCard card) {
        mCard = card;
    }

    public List<GKFile> listFiles(String remotePath) throws IOException {
        CardClient.Response response = mCard.list(remotePath);
        byte[] bytes = response.getData();
        List<GKFile> files = parseFileList(bytes);
        for (GKFile file : files) {
            file.setCardPath(remotePath, file.getName());
        }
        return files;
    }

    public File getFile(final GKFile gkFile, File localFile) throws IOException {
        CardClient.Response response = mCard.retrieve(gkFile.getCardPath());
        FileOutputStream outputStream = new FileOutputStream(localFile);
        outputStream.write(response.getData());
        return localFile;
    }

    public boolean putFile(InputStream localFile, String remotePath) throws IOException {
        CardClient.Response response = mCard.store(remotePath, localFile);
        return response.getStatus() == 226;
    }

    public boolean deleteFile(GKFile file) throws IOException {
        if (file.getType() == GKFile.Type.FILE) {
            return mCard.deleteFile(file.getCardPath());
        } else {
            return mCard.removeDirectory(file.getCardPath());
        }
    }

    public boolean makeDirectory(String fullPath) throws IOException {
        return mCard.makeDirectory(fullPath);
    }

    public String getRootPath() {
        return mCard.getRootPath();
    }

    private final Pattern mFilePattern = Pattern.compile("([-d])\\S+(\\S+\\s+){8}(.*)$");

    private List<GKFile> parseFileList(byte[] response) {
        String responseString = new String(response);

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseString);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }

        List<GKFile> filesList = new ArrayList<>();

        for (String fileString : list) {
            Matcher fileMatcher = mFilePattern.matcher(fileString);
            if (fileMatcher.find()) {
                String typeString = fileMatcher.group(1);
                String name = fileMatcher.group(3);
                GKFile.Type type = typeString.equals("d") ? GKFile.Type.DIRECTORY : GKFile.Type.FILE;
                GKFile file = new GKFile(name, type);
                filesList.add(file);
            }
        }

        return filesList;
    }
}
