package se.sugarest.jane.popularmovies.utilities;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by jane on 17-8-15.
 */

/**
 * Utility file for setting up Timber.
 */
public class TimberUtils extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Set up (Plant) Timber.
        Timber.plant(new Timber.DebugTree() {
            // Add the line number to the tag.
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });
    }
}
