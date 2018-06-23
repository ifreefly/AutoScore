import java.io.File;
import java.io.IOException;

public class Test {

    @org.junit.Test
    public void test001() throws IOException {
        String zipString = "wubin-00291812-通用软件2级.zip";
        System.out.println(zipString.substring(0, zipString.length() - 4));

        File file = new File("E:\\tmp\\debug\\mydebug");
        String path = file.getCanonicalPath();
        System.out.println(path);

        File subFile = new File("E:\\tmp\\debug\\mydebug");
        String subPath = subFile.getCanonicalPath();
        System.out.println(subPath);

        System.out.println(subPath.substring(path.length(), subPath.length()));
    }
}
