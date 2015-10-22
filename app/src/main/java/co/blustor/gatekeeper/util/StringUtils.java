package co.blustor.gatekeeper.util;

import java.util.List;

public class StringUtils {
    public static String join(List<String> strings, String separator) {
        if (strings.size() == 0) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(strings.get(0));
        for (int i = 1; i < strings.size(); i++) {
            sb.append(separator);
            sb.append(strings.get(i));
        }
        return sb.toString();
    }
}
