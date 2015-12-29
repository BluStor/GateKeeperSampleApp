package co.blustor.gatekeeper.scopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.data.GKFile;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

public class GKFileActions {
    public static final String TAG = GKFileActions.class.getSimpleName();

    private final GKCard mCard;

    public GKFileActions(GKCard card) {
        mCard = card;
    }

    public List<GKFile> listFiles(String remotePath) throws IOException {
        Response response = mCard.list(remotePath);
        byte[] bytes = response.getData();
        List<GKFile> files = parseFileList(bytes);
        for (GKFile file : files) {
            file.setCardPath(remotePath, file.getName());
        }
        return files;
    }

    public File getFile(final GKFile gkFile, File localFile) throws IOException {
        Response response = mCard.get(gkFile.getCardPath());
        FileOutputStream outputStream = new FileOutputStream(localFile);
        outputStream.write(response.getData());
        return localFile;
    }

    public boolean putFile(InputStream localFile, String remotePath) throws IOException {
        Response response = mCard.put(remotePath, localFile);
        return response.getStatus() == 226;
    }

    public boolean deleteFile(GKFile file) throws IOException {
        if (file.getType() == GKFile.Type.FILE) {
            Response response = mCard.delete(file.getCardPath());
            return response.getStatus() == 250;
        } else {
            Response response = mCard.deletePath(file.getCardPath());
            return response.getStatus() == 250;
        }
    }

    public boolean makeDirectory(String fullPath) throws IOException {
        Response response = mCard.createPath(fullPath);
        return response.getStatus() == 257;
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
