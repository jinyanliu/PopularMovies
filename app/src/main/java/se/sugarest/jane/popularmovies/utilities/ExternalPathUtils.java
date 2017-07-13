package se.sugarest.jane.popularmovies.utilities;

/**
 * Created by jane on 17-7-11.
 */

import android.content.Context;

/**
 * Utility functions to handle external path in different API level.
 */
public class ExternalPathUtils {

    private static final String TAG = ExternalPathUtils.class.getSimpleName();

    public static String getExternalPathBasicFileName(Context context) {
        // Don't write into sd card, write into cache folder for Android apps.
        // So when the users uninstall the app, the folder will be deleted either.
        // If you hard write into the sd card, the folder will always be there unless the user delete
        // them manually.
        return context.getExternalCacheDir().getAbsolutePath();
    }
}
