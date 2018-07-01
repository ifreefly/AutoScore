package idevcod.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtil.class);

    private static final int BUFFER_SIZE = 4096;

    public static boolean unzip(final String src, final String des) {
        if (StringUtil.isEmpty(src)) {
            throw new IllegalArgumentException("src is null or empty str!");
        }

        File srcFile = new File(src);
        if (!srcFile.exists() || srcFile.isDirectory()) {
            LOGGER.error("src not exist or srcFile is directory");
            return false;
        }

        String zipDes = des;
        if (StringUtil.isEmpty(zipDes)) {
            try {
                zipDes = srcFile.getParentFile().getCanonicalPath();
            } catch (IOException e) {
                LOGGER.error("get des directory failed, error is ", e);
                return false;
            }
        }

        if (!createDirectoryIfNotExist(zipDes)) {
            return false;
        }

        return unzip(srcFile, zipDes);
    }

    private static boolean unzip(File srcFile, String zipDes) {
        try (FileInputStream fileInputStream = new FileInputStream(srcFile);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            while (true) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null) {
                    break;
                }

                String filePath = zipDes + File.separator + zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    extractFile(zipInputStream, filePath);
                } else {
                    File zipDirectory = new File(filePath);
                    if (!zipDirectory.mkdir()) {
                        LOGGER.warn("mkdir failed, path is {}", filePath);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("unzip file failed.");
            return false;
        }

        return true;
    }

    private static void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)
             ; BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)) {
            byte[] bytes = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipInputStream.read(bytes)) != -1) {
                bos.write(bytes, 0, read);
            }
        }
    }

    private static boolean createDirectoryIfNotExist(String zipDesc) {
        File desFile = new File(zipDesc);
        if (!desFile.exists()) {
            return desFile.mkdirs();
        }

        if (desFile.isFile()) {
            LOGGER.error("create des directory failed, des file exist.");
            return false;
        }

        return true;
    }
}
