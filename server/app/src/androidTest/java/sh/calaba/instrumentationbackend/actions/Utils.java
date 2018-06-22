package sh.calaba.instrumentationbackend.actions;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static void copyContents(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[4*1024];

        int read;

        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
        }
    }

    public static boolean isApplicationInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
