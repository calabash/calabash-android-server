package sh.calaba.instrumentationbackend;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Cleaner {

    public void clearAppData(Context targetContext) {
        System.out.println("External cache dir: " + externalCacheDir(targetContext));

        if (externalCacheDir(targetContext) != null) {
            System.out.println("Deleting external cache dir...");
            delete(externalCacheDir(targetContext));
        }

        System.out.println("Cache dir: " + cacheDir(targetContext));

        if (cacheDir(targetContext) != null) {
            System.out.println("Deleting cache dir...");
            delete(cacheDir(targetContext));
        }

        System.out.println("External files dir: " + externalFilesDir(targetContext));

        if (externalFilesDir(targetContext) != null) {
            System.out.println("Deleting external files dir...");
            delete(externalFilesDir(targetContext));
        }

        System.out.println("Files dir: " + filesDir(targetContext));

        if (filesDir(targetContext) != null) {
            System.out.println("Deleting files dir...");
            delete(filesDir(targetContext));
        }

        System.out.println("Data dir: " + dataDir(targetContext));

        // Delete everything but lib in the application sandbox
        for (File file : dataDir(targetContext).listFiles()) {
            if (!"lib".equals(file.getName())) {
                delete(file);
            }
        }

        removeOwnAccountTypes(targetContext);
    }

    private void removeOwnAccountTypes(Context targetContext) {
        if (targetContext.checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            final AccountManager manager = AccountManager.get(targetContext);
            final Account[] accounts = manager.getAccounts();
            final List<String> typesToDelete = new ArrayList<String>();

            for (Account account : accounts) {
                System.out.println("Found " + account.name + " of type " + account.type);

                Account dummy = new Account("dummy", account.type);

                try {
                    // If we can add a new account, we own the account type
                    manager.addAccountExplicitly(dummy, "", null);
                    typesToDelete.add(account.type);
                } catch (SecurityException e) {
                    // We do not own the account type
                }
            }

            for (Account account : accounts) {
                if (typesToDelete.contains(account.type)) {
                    try {
                        System.out.println("Deleting " + account.name + " of type " + account.type + "...");
                        manager.removeAccount(account, null, null);
                    } catch (SecurityException e) {
                        System.out.println("Unable to delete account");
                    }
                }
            }
        }
    }

    // If provided a file will delete it.
    // If provided a directory will recursively delete files but preserve directories
    // Will never delete mono runtime files!
    private void delete(File fileOrDirectory) {
        if (fileOrDirectory == null) {
            return;
        }

        // Don't delete mono runtime
        if (".__override__".equals(fileOrDirectory.getName())) {
            return;
        }

        if (fileOrDirectory.isDirectory()) {
            if (fileOrDirectory.listFiles() != null) {
                for(File f : fileOrDirectory.listFiles()) {
                    delete(f);
                }
            }

            if (fileOrDirectory.listFiles().length == 0) {
                System.out.println("Deleting: " + fileOrDirectory);
                fileOrDirectory.delete();
            }
        } else {
            System.out.println("Deleting: " + fileOrDirectory);
            fileOrDirectory.delete();
        }
    }

    private File cacheDir(Context targetContext) {
        return targetContext.getCacheDir();
    }

    private File externalCacheDir(Context targetContext) {
        if (Build.VERSION.SDK_INT >= 8) {
            return targetContext.getExternalCacheDir();
        } else {
            return null;
        }
    }

    private File filesDir(Context targetContext) {
        return targetContext.getFilesDir();
    }

    private File externalFilesDir(Context targetContext) {
        File externalCacheDir = externalCacheDir(targetContext);

        if (externalCacheDir == null) {
            return null;
        }

        File parentDir = externalCacheDir.getParentFile();

        if (parentDir == null) {
            return null;
        }

        return new File(parentDir, "files");
    }

    private File dataDir(Context targetContext) {
        try {
            PackageManager packageManager = targetContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(targetContext.getPackageName(), 0);

            return new File(packageInfo.applicationInfo.dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
