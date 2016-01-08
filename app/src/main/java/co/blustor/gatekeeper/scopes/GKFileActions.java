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

    public enum Status {
        SUCCESS,
        UNAUTHORIZED,
        NOT_FOUND,
        UNKNOWN_STATUS
    }

    private final GKCard mCard;

    public GKFileActions(GKCard card) {
        mCard = card;
    }

    public ListFilesResult listFiles(String cardPath) throws IOException {
        Response response = mCard.list(cardPath);
        return new ListFilesResult(response, cardPath);
    }

    public GetFileResult getFile(final GKFile gkFile, File localFile) throws IOException {
        Response response = mCard.get(gkFile.getCardPath());
        GetFileResult result = new GetFileResult(response, localFile);
        if (result.getStatus() == Status.SUCCESS) {
            FileOutputStream outputStream = new FileOutputStream(localFile);
            outputStream.write(response.getData());
        }
        return result;
    }

    public FileResult putFile(InputStream localFile, String cardPath) throws IOException {
        Response response = mCard.put(cardPath, localFile);
        if (response.getStatus() != 226) {
            return new FileResult(response);
        }
        Response finalize = mCard.finalize(cardPath);
        return new FileResult(finalize);
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

    public class ListFilesResult extends FileResult {
        protected final List<GKFile> mFiles;

        public ListFilesResult(Response response, String cardPath) {
            super(response);
            mFiles = parseFileList(response.getData(), cardPath);
        }

        public List<GKFile> getFiles() {
            return mFiles;
        }

        private List<GKFile> parseFileList(byte[] response, String cardPath) {
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
                    file.setCardPath(cardPath, file.getName());
                    filesList.add(file);
                }
            }

            return filesList;
        }
    }

    public class GetFileResult extends FileResult {
        protected final File mFile;

        public GetFileResult(Response response, File file) {
            super(response);
            mFile = file;
        }

        public File getFile() {
            return mFile;
        }
    }

    public class FileResult {
        protected final Response mResponse;
        protected final Status mStatus;

        public FileResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(response);
        }

        public Status getStatus() {
            return mStatus;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 213:
                return Status.SUCCESS;
            case 226:
                return Status.SUCCESS;
            case 530:
                return Status.UNAUTHORIZED;
            case 550:
                return Status.NOT_FOUND;
            default:
                return Status.UNKNOWN_STATUS;
        }
    }
}
