package co.blustor.gatekeeper.bftp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.data.GKFile;

public class FTPResponseParser {
    public static final String TAG = FTPResponseParser.class.getSimpleName();

    private final Pattern mFilePattern = Pattern.compile("([-d]).* (.*)$");

    public GKFile[] parseListResponse(byte[] response) {
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
                String name = fileMatcher.group(2);
                GKFile.TYPE type = typeString.equals("d") ? GKFile.TYPE.DIRECTORY : GKFile.TYPE.FILE;
                GKFile file = new GKFile(name, type);
                filesList.add(file);
            }
        }

        GKFile[] filesArray = new GKFile[filesList.size()];
        return filesList.toArray(filesArray);
    }
}
