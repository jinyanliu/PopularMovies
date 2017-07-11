package se.sugarest.jane.popularmovies.utilities;

/**
 * Created by jane on 17-7-11.
 */

import android.content.Context;
import android.os.Build;
import android.os.Environment;

/**
 * Utility functions to handle external path in different API level.
 */
public class ExternalPathUtils {

    private static final String TAG = ExternalPathUtils.class.getSimpleName();

    public static String getExternalPathBasicFileName(Context context) {
        String externalPathBasicFileName;
        // API 24 Android 7.0 Nougat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            externalPathBasicFileName = context.getExternalCacheDir().getAbsolutePath();
        } else {
            externalPathBasicFileName = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath();
        }
        return externalPathBasicFileName;
    }
}
