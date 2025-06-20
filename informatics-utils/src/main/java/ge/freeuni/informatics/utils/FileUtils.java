package ge.freeuni.informatics.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

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

    public static void extractRar(String rarFilePath, String outputDir) throws IOException {
        File rarFile = new File(rarFilePath);
        File destinationFolder = new File(outputDir);

        if (!destinationFolder.exists()) {
            if (!destinationFolder.mkdirs()) {
                throw new IOException("Failed to create output directory: " + outputDir);
            }
        }

        try (Archive archive = new Archive(rarFile)) {
            if (archive == null) {
                throw new IOException("Invalid RAR file: " + rarFilePath);
            }

            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                String fileName = fileHeader.getFileNameString().trim();
                File outputFile = new File(destinationFolder, fileName);

                if (fileHeader.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + outputFile.getAbsolutePath());
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
                    }

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        archive.extractFile(fileHeader, fos);
                    }
                }
            }
        } catch (RarException e) {
            throw new IOException("Error extracting RAR file: " + rarFilePath, e);
        }
    }

    public static String unzip(String zipAddress) throws IOException {
        String destDir = zipAddress.substring(0, zipAddress.length() - 4);
        if (isRAR(zipAddress)) {
            extractRar(zipAddress, destDir);
            return destDir;
        }
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

    public static boolean isRAR(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] signature = new byte[4];
            if (fis.read(signature) != 4) {
                throw new IOException("unsupportedArchive");
            }

            // Check for ZIP signature
            if (signature[0] == 0x50 && signature[1] == 0x4B) {
                return false;
            }

            // Check for RAR signature
            if (signature[0] == 0x52 && signature[1] == 0x61 && signature[2] == 0x72 && signature[3] == 0x21) {
                return true;
            }
            throw new IOException("unsupportedArchive");
        }
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

    public static void cleanDirectory(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Provided file is not a directory: " + dir);
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return; // Directory is empty or inaccessible
        }

        for (File file : files) {
            if (file.isDirectory()) {
                cleanDirectory(file); // Recursively clean subdirectories
                if (!file.delete()) {
                    throw new IOException("Failed to delete directory: " + file.getAbsolutePath());
                }
            } else {
                if (!file.delete()) {
                    throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
    }
}
