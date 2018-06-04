package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static String getTimeStamp(long millis) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(new Date(millis));
    }
}
