package sh.calaba.instrumentationbackend.actions.l10n;

import android.app.Application;
import sh.calaba.instrumentationbackend.InstrumentationBackend;

/**
 * Helper to access Android L10n files.
 * 
 * @author Dominik Dary
 * 
 */
public class L10nHelper {
  /**
   * get the translated value based on the current active locale.
   * 
   * @param l10nKey The l10n key to use
   * @param pckg Optional package to find the resource, defaults to the application's package if null
   * @return The translated value.
   */
  public static String getValue(String l10nKey, String pckg) {
    Application application = InstrumentationBackend.getDefaultCalabashAutomation().getCurrentApplication();

    if (application == null) {
        throw new RuntimeException("Application is null");
    }

    if (pckg == null) {
        pckg = application.getPackageName();
    }
      
    int resourceId =
        application
            .getResources()
            .getIdentifier(l10nKey, "string", pckg);

    return application.getResources().getString(resourceId);
  }
}
