package idevcod.util;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RarUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RarUtil.class);

    public static boolean unrarFile(String src, String des) {
        if (StringUtil.isEmpty(src)) {
            throw new IllegalArgumentException("src is null or empty str!");
        }

        File srcFile = new File(src);
        if (!srcFile.exists() || srcFile.isDirectory()) {
            LOGGER.error("src not exist or srcFile is directory");
            return false;
        }

        String rarDes = des;
        if (StringUtil.isEmpty(rarDes)) {
            try {
                rarDes = srcFile.getParentFile().getCanonicalPath();
            } catch (IOException e) {
                LOGGER.error("get des directory failed, error is ", e);
                return false;
            }
        }

        if (!FileUtil.createDirectoryIfNotExist(rarDes)) {
            return false;
        }

        try (Archive archive = new Archive(srcFile, null)) {
            if (archive.isEncrypted()) {
                LOGGER.warn("rar is encrypted! can not extract");
                return false;
            }

            while (true) {
                FileHeader fileHeader = archive.nextFileHeader();
                if (fileHeader == null) {
                    break;
                }

                if (fileHeader.isEncrypted()) {
                    LOGGER.warn("fileHeader {} in rar is encrypted, can not extract!", fileHeader.getFileNameString());
                    continue;
                }

                String rarName = getRarName(fileHeader);
                String filePath = rarDes + File.separator + rarName;

                if (!fileHeader.isDirectory()) {
                    extractFile(archive, fileHeader, filePath);
                } else {
                    if (!FileUtil.createDirectoryIfNotExist(filePath)) {
                        LOGGER.warn("mkdir failed, path is {}", filePath);
                    }
                }
            }
        } catch (RarException | IOException e) {
            LOGGER.error("unrar failed, exception is ", e);
            return false;
        }

        return true;
    }

    private static void extractFile(Archive archive, FileHeader fileHeader, String filePath) throws IOException, RarException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!FileUtil.createDirectoryIfNotExist(parent.getCanonicalPath())) {
            return;
        }

        try (FileOutputStream outputStream = new FileOutputStream(new File(filePath))) {
            archive.extractFile(fileHeader, outputStream);
        }
    }

    private static String getRarName(FileHeader fileHeader) {
        if (fileHeader.isUnicode()) {
            return fileHeader.getFileNameW();
        }

        return fileHeader.getFileNameString();
    }
}
