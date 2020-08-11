package org.bonitasoft.maven.dependency.util;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for zip/unzip operations
 */
public final class ZipUtil {

    private static final int BUFFER_SIZE = 4096;

    private ZipUtil() {
        // Utility class
    }

    /**
     * Unzip file in the target directory
     *
     * @param file      the file to unzip
     * @param targetDir the target directory where to unzip the file
     * @return the path where the file has been unzipped
     * @throws IOException
     */
    public static Path unzip(File file, Path targetDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte[] data = new byte[BUFFER_SIZE];
                File target = targetDir.toFile().toPath().resolve(entry.getName()).toFile();
                if (entry.isDirectory()) {
                    target.mkdirs();
                } else {
                    if (!target.exists()) {
                        target.getParentFile().mkdirs();
                        if (!target.createNewFile()) {
                            throw new IOException("Failed to create file " + target.getAbsolutePath());
                        }
                    }
                    try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(target), BUFFER_SIZE);) {
                        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                    }
                }
            }
        }
        return targetDir;
    }
}
