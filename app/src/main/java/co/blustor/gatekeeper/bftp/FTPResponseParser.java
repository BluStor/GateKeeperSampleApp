package co.blustor.gatekeeper.bftp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.ftp.FTPFile;

public class FTPResponseParser {
    public static final String TAG = FTPResponseParser.class.getSimpleName();

    private final Pattern mFilePattern = Pattern.compile("([-d]).* (.*)$");

    public FTPFile[] parseListResponse(byte[] response) {
        String responseString = new String(response);

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseString);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }

        List<FTPFile> filesList = new ArrayList<>();

        for (String fileString : list) {
            Matcher fileMatcher = mFilePattern.matcher(fileString);
            if (fileMatcher.find()) {
                String typeString = fileMatcher.group(1);
                String name = fileMatcher.group(2);
                FTPFile.TYPE type = typeString.equals("d") ? FTPFile.TYPE.DIRECTORY : FTPFile.TYPE.FILE;
                FTPFile file = new FTPFile(name, type);
                filesList.add(file);
            }
        }

        FTPFile[] filesArray = new FTPFile[filesList.size()];
        return filesList.toArray(filesArray);
    }
}
