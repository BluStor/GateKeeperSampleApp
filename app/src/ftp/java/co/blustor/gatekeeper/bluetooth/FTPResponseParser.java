package co.blustor.gatekeeper.bluetooth;


import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPResponseParser {
    private static final String TAG = "FTPResponseParser";

    public FTPFile[] parseListResponse(byte[] response) {
        String responseString = new String(response);

        Pattern p = Pattern.compile(".*\r\n");
        Matcher m = p.matcher(responseString);

        List<String> list = new ArrayList<String>();

        while(m.find()) {
            list.add(m.group());
        }

        List<FTPFile> files = new ArrayList<>();

        // TODO: Fix for spaces in file name...
        Pattern filePattern = Pattern.compile("([-d]).* (.*)$");
        for(String fileString : list) {
            Matcher fileMatcher = filePattern.matcher(fileString);
            if(fileMatcher.find()) {
                String type = fileMatcher.group(1);
                String name = fileMatcher.group(2);
                FTPFile file = new FTPFile();

                if(type.equals("d"))
                    file.setType(FTPFile.DIRECTORY_TYPE);
                else
                    file.setType(FTPFile.FILE_TYPE);

                file.setName(name);

                files.add(file);
            }
        }

        FTPFile[] filesArray = new FTPFile[files.size()];
        int i = 0;
        for(FTPFile file : files) {
            filesArray[i] = file;
            i++;
        }

        return filesArray;
    }
}
