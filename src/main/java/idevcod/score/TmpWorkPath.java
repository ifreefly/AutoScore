package idevcod.score;

import idevcod.util.FileUtil;
import idevcod.util.RarUtil;
import idevcod.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Queue;

class TmpWorkPath {
    private static final Logger LOGGER = LoggerFactory.getLogger(TmpWorkPath.class);

    private static final String ZIP_POSTFIX = ".zip";

    private static final String RAR_POSTFIX = ".rar";

    private String tmpWorkPath;

    private String libPath;

    private String srcPath;

    private String testPath;

    private String buildFilePath;

    TmpWorkPath(String tmpWorkPath) {
        this.tmpWorkPath = tmpWorkPath;
        this.libPath = tmpWorkPath + File.separator + "lib";
        this.srcPath = tmpWorkPath + File.separator + "src";
        this.testPath = tmpWorkPath + File.separator + "test";

        this.buildFilePath = this.tmpWorkPath + File.separator + "build.xml";
    }

    boolean prepareTmpWorkDir(WorkPath workPath, File examFile) throws IOException {
        if (!cleanTmpWorkDir()) {
            return false;
        }

        if (!extractFile(examFile, workPath.getTmpPath())) {
            LOGGER.error("extract exam failed.");
            return false;
        }

        if (!clearExamTest()) {
            LOGGER.error("clear exam test dir failed");
            return false;
        }

        if (!copyTemplateBuildFile(workPath.getTemplateBuildFilePath())) {
            LOGGER.error("copyTemplateBuildFile failed");
            return false;
        }

        if (!validateSrcPath()) {
            LOGGER.error("validate src path failed");
            return false;
        }

        if (!validateLibPath()) {
            LOGGER.error("validate lib path failed failed");
            return false;
        }

        copyUsecase(workPath.getUseCasePath(), workPath.getTmpPath());

        return true;
    }

    private boolean validateSrcPath() throws IOException {
        File srcDirectory = new File(srcPath);
        if (srcDirectory.isDirectory()) {
            return true;
        }

        if (srcDirectory.exists()) {
            LOGGER.warn("srcFile exist, but was not directory!");
            return false;
        }

        if (!findSpecificDirAndCopy("src")) {
            LOGGER.warn("find src dir and copy failed");
            return false;
        }

        return true;
    }

    private boolean findSpecificDirAndCopy(String specificDirName) throws IOException {
        File rootDirectory = new File(tmpWorkPath);
        Queue<File> queue = new LinkedList<>();

        queue.offer(rootDirectory);
        for (File dir = queue.poll(); dir != null; dir = queue.poll()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return false;
            }

            for(File file : files) {
                if (!file.isDirectory()) {
                    continue;
                }

                if (!specificDirName.equals(file.getName())) {
                    queue.offer(file);
                    continue;
                }

                FileUtil.copyDirectory(file.getCanonicalPath(), tmpWorkPath);
                return true;
            }
        }

        return false;
    }

    private boolean cleanTmpWorkDir() {
        File tmpWorkFile = new File(tmpWorkPath);
        if (!tmpWorkFile.exists()) {
            return true;
        }

        if (!FileUtil.deleteDir(tmpWorkFile)) {
            LOGGER.error("delete tmp path failed");
            return false;
        }

        if (!createDir(tmpWorkPath)) {
            LOGGER.error("create tmp path failed");
            return false;
        }

        return true;
    }

    private boolean extractFile(File file, String desPath) throws IOException {
        String fullFileName = file.getName();
        if (fullFileName.endsWith(ZIP_POSTFIX)) {
            return ZipUtil.unzip(file.getCanonicalPath(), desPath);
        }

        if (fullFileName.endsWith(RAR_POSTFIX)) {
            return RarUtil.unrarFile(file.getCanonicalPath(), desPath);
        }

        return true;
    }

    private boolean copyTemplateBuildFile(String srcBuildFilePath) {
        File srcBuildFile = new File(srcBuildFilePath);
        File desBuildFile = new File(buildFilePath);

        try {
            Files.copy(srcBuildFile.toPath(), desBuildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("copy build File {} failed.", srcBuildFile, e);
            return false;
        }

        return true;
    }

    private void copyUsecase(String useCasePath, String tmpPath) throws IOException {
        FileUtil.copyDirectory(useCasePath, tmpPath);
    }

    private boolean clearExamTest() {
        File testDirectory = new File(testPath);
        if (!testDirectory.exists()) {
            return true;
        }

        if (testDirectory.isDirectory()) {
            if (!FileUtil.deleteDir(testDirectory)) {
                LOGGER.warn("delete test dir failed");
                return false;
            }

            return true;
        }

        if (!testDirectory.delete()) {
            LOGGER.warn("delete test file failed");
            return false;
        }

        return true;
    }

    private boolean validateLibPath() throws IOException {
        File libDirectory = new File(libPath);
        if (libDirectory.isDirectory()) {
            return true;
        }

        if (libDirectory.exists()) {
            LOGGER.warn("libFile exist, but was not directory!");
            return false;
        }

        if (findSpecificDirAndCopy("lib")) {
            return true;
        }

        if (libDirectory.mkdir()) {
            return true;
        }

        LOGGER.warn("validateLibPath failed");

        return false;
    }

    private boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdirs();
    }

    String getTmpWorkPath() {
        return tmpWorkPath;
    }

    String getBuildFilePath() {
        return buildFilePath;
    }
}
