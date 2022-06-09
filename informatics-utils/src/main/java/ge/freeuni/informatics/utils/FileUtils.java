package ge.freeuni.informatics.utils;

import java.util.List;

public class FileUtils {

    public static String buildPath(List<String> pathParts) {
        StringBuilder path = new StringBuilder();

        for (String part: pathParts) {
            if (path.length() > 0) {
                path.append("/");
            }
            if (part.charAt(0) == '/') {
                path.append(part.substring(1));
            } else {
                path.append(part);
            }
            if (path.charAt(part.length() - 1) == '/') {
                path.deleteCharAt(path.length() - 1);
            }
        }
        return path.toString();
    }
}
