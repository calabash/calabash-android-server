package sh.calaba.instrumentationbackend.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class StringUtils {

    private StringUtils() {}

    public static String toString(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        th.printStackTrace(new PrintWriter(stringWriter));

        return stringWriter.toString();
    }
}
