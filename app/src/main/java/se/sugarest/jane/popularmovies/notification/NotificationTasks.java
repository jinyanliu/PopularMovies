package se.sugarest.jane.popularmovies.notification;

import android.content.Context;
import android.util.Log;

/**
 * Created by jane on 17-8-30.
 */

public class NotificationTasks {

    private final static String TAG = NotificationTasks.class.getSimpleName();

    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final String ACTION_NOTIFY = "notify";

    public static void executeTask(Context context, String action) {
        if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            Log.i(TAG, "jag dismiss notify!");
            NotificationUtils.clearAllNotifications(context);
        } else if (ACTION_NOTIFY.equals(action)) {
            Log.i(TAG, "jag notify !");
            notifyUser(context);
        }
    }

    private static void notifyUser(Context context) {
        NotificationUtils.notifyUserHighestRatePopularMovie(context);
    }
}
