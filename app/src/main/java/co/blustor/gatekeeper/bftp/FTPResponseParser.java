package co.blustor.gatekeeper.bftp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.ftp.FTPFile;

public class FTPResponseParser {
    public static final String TAG = FTPResponseParser.class.getSimpleName();

    public FTPFile[] parseListResponse(byte[] response) {
        String responseString = new String(response);

        Pattern p = Pattern.compile(".*\r\n");
        Matcher m = p.matcher(responseString);

        List<String> list = new ArrayList<>();

        while (m.find()) {
            list.add(m.group());
        }

        List<FTPFile> filesList = new ArrayList<>();

        Pattern filePattern = Pattern.compile("([-d]).* (.*)$");
        for (String fileString : list) {
            Matcher fileMatcher = filePattern.matcher(fileString);
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
