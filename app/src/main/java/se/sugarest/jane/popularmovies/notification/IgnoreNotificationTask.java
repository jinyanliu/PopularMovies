package se.sugarest.jane.popularmovies.notification;

import android.content.Context;

/**
 * Created by jane on 17-8-30.
 */

public class IgnoreNotificationTask {
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final String ACTION_NOTIFY = "notify";

    public static void executeTask(Context context, String action) {
        if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            NotificationUtils.clearAllNotifications(context);
        } else if (ACTION_NOTIFY.equals(action)) {
            notifyUser(context);
        }
    }

    private static void notifyUser(Context context) {
        NotificationUtils.notifyUserHighestRatePopularMovie(context);
    }
}
