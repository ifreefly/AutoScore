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

public class TmpWorkPath {
    private static final Logger LOGGER = LoggerFactory.getLogger(TmpWorkPath.class);

    private static final String ZIP_POSTFIX = ".zip";

    private static final String RAR_POSTFIX = ".rar";

    private String tmpWorkPath;

    private String libPath;

    private String testPath;

    private String buildFilePath;

    TmpWorkPath(String tmpWorkPath) {
        this.tmpWorkPath = tmpWorkPath;
        this.libPath = tmpWorkPath + File.separator + "lib";
        this.testPath = tmpWorkPath + File.separator + "test";

        this.buildFilePath = this.tmpWorkPath + File.separator + "build.xml";
    }

    boolean prepareTmpWorkDir(WorkPath workPath, File examFile) throws IOException {
        String examPaperName = examFile.getName();

        if (!cleanTmpWorkDir()) {
            return false;
        }

        if (!extractFile(examFile, workPath.getTmpPath())) {
            LOGGER.error("score {} failed, extract exam failed.", examPaperName);
            return false;
        }

        if (!clearExamTest()) {
            LOGGER.error("score {} failed, clear exam test dir failed", examPaperName);
            return false;
        }

        if (!copyTemplateBuildFile(workPath.getTemplateBuildFilePath())) {
            LOGGER.error("score {} failed, copyTemplateBuildFile failed", examPaperName);
            return false;
        }

        if (!validateLibPath()) {
            LOGGER.error("score {} failed, validate lib path failed failed", examPaperName);
            return false;
        }

        copyUsecase(workPath.getUseCasePath(), workPath.getTmpPath());

        return true;
    }

    private boolean cleanTmpWorkDir() {
        File tmpWorkFile = new File(tmpWorkPath);
        if (!tmpWorkFile.exists()) {
            return true;
        }

        if (!FileUtil.deleteDir(tmpWorkFile)) {
            LOGGER.error("score {} failed, delete tmp path failed", tmpWorkPath);
            return false;
        }

        if (!createDir(tmpWorkPath)) {
            LOGGER.error("score {} failed, create tmp path failed", tmpWorkPath);
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

        LOGGER.error("score {} failed, postfix is not zip or rar!", fullFileName);

        return true;
    }

    private boolean copyTemplateBuildFile(String srcBuildFilePath) {
        File srcBuildFile = new File(srcBuildFilePath);
        File desBuildFile = new File(buildFilePath);

        try {
            Files.copy(srcBuildFile.toPath(), desBuildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("copy build File failed.", e);
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

    private boolean validateLibPath() {
        File libDirectory = new File(libPath);
        if (libDirectory.isFile()) {
            LOGGER.warn("lib path is a file");
            return false;
        }

        if (libDirectory.exists()) {
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

    public String getTmpWorkPath() {
        return tmpWorkPath;
    }

    String getBuildFilePath() {
        return buildFilePath;
    }
}
