package ge.freeuni.informatics.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static String buildPath(String... pathParts) {
        StringBuilder path = new StringBuilder();

        for (String part: pathParts) {
            path.append("/");
            if (part.charAt(0) == '/') {
                path.append(part.substring(1));
            } else {
                path.append(part);
            }
            if (path.charAt(path.length() - 1) == '/') {
                path.deleteCharAt(path.length() - 1);
            }
        }
        return path.toString();
    }

    public static String unzip(String zipAddress) throws IOException {
        String destDir = zipAddress.substring(0, zipAddress.length() - 4);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipAddress)));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            while (zipEntry != null) {
                File newFile = newFile(new File(destDir), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
        }
        zis.closeEntry();
        zis.close();
        return destDir;
    }

    public static String getRandomFileName(int length) {
        return StringUtils.getRandomBase64String(length).replaceAll("/", "A");
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
