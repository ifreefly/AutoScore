package idevcod.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Stack;

public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static boolean deleteDir(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            throw new IllegalArgumentException("root is null or empty");
        }

        File root = new File(dirPath);
        if (root.isFile()) {
            throw new UnsupportedOperationException(dirPath + " is not root, it's file!");
        }

        return deleteDir(root);
    }

    public static boolean deleteDir(File rootDir) {
        Stack<File> dirs = new Stack<>();
        Stack<File> dirsToBeDelete = new Stack<>();

        dirs.push(rootDir);
        dirsToBeDelete.push(rootDir);

        while (!dirs.isEmpty()) {
            File dir = dirs.pop();

            File[] subFiles = dir.listFiles();
            if (subFiles == null) {
                if (!dir.delete()) {
                    LOGGER.error("delete {} failed", dir.getName());
                    return false;
                }

                continue;
            }

            for (File file : subFiles) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        LOGGER.error("delete {} failed", file.getName());
                        return false;
                    }
                } else {
                    dirs.push(file);
                    dirsToBeDelete.push(file);
                }
            }
        }

        while (!dirsToBeDelete.isEmpty()) {
            File dirToBeDelete = dirsToBeDelete.pop();
            if (!dirToBeDelete.delete()) {
                return false;
            }
        }

        return true;
    }

    public static void copyDirectory(String srcDir, final String desDir) throws IOException {
        if (StringUtil.isEmpty(srcDir)) {
            throw new IllegalArgumentException("srcDir is null or empty");
        }

        if (StringUtil.isEmpty(desDir)) {
            throw new IllegalArgumentException("desDir is null or empty");
        }

        File srcFile = new File(srcDir);
        if (!srcFile.isDirectory()) {
            throw new IllegalStateException("srcDir " + srcDir + " is not directory!");
        }

        String realDesDir = desDir;
        File desFile = new File(desDir);
        if (desFile.exists() ) {
            if (!desFile.isDirectory()) {
                throw new IllegalStateException("desDir " + desDir + " is not directory");
            }

            realDesDir += File.separator + srcFile.getName();
        } else {
            if (!desFile.mkdir()) {
                throw new IllegalStateException("mkdir desDir " + desDir + " failed");
            }
        }

        String srcCanonicalPath = srcFile.getCanonicalPath();

        Stack<File> dirs = new Stack<>();
        dirs.push(srcFile);

        while (!dirs.isEmpty()) {
            File dir = dirs.pop();

            File[] subFiles = dir.listFiles();
            if (subFiles == null) {
                continue;
            }

            for (File subFile : subFiles) {
                String subFileCanonicalPath = subFile.getCanonicalPath();
                File desSubFile = new File(realDesDir + subFileCanonicalPath.substring(srcCanonicalPath.length(),
                        subFileCanonicalPath.length()));

                if (subFile.isFile()) {
                    Files.copy(subFile.toPath(), desSubFile.toPath());
                } else {
                    desSubFile.mkdirs();
                    dirs.push(subFile);
                }
            }
        }
    }
}
