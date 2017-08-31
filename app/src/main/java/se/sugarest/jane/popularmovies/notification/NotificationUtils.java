package se.sugarest.jane.popularmovies.notification;

/**
 * Created by jane on 17-8-30.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.ui.DetailActivity;
import se.sugarest.jane.popularmovies.ui.MainActivity;

/**
 * Utility class for creating nitofications
 */
public class NotificationUtils {

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 135 is in no way significant.
     */
    private static final int HIGHEST_RATE_POP_MOVIE_NOTIFICATION_ID = 135;
    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int HIGHEST_RATE_POP_MOVIE_PENDING_INTENT_ID = 357;
    private static final int ACTION_GO_TO_PAGE_PENDING_INTENT_ID = 579;
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 7911;

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void notifyUserHighestRatePopularMovie(Context context) {

        Movie movie = getHighestRatePopMovie(context);

        String movie_title = movie.getOriginalTitle();
        String movie_rate = movie.getUserRating();

        String contentText = String.format(context.getString(R.string.format_notification), movie_title, movie_rate);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_small_icon)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.highest_rate_pop_movie_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(openDetailActivityAction(context, movie))
                .addAction(ignoreNotificationAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        /* HIGHEST_RATE_POP_MOVIE_NOTIFICATION_ID allows you to update or cancel the notification later on */
        notificationManager.notify(HIGHEST_RATE_POP_MOVIE_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static Movie getHighestRatePopMovie(Context context) {

        Movie movie = null;

        String sortOrder = CacheMovieMostPopularEntry.COLUMN_USER_RATING + " DESC";

        Cursor cursor = context.getContentResolver().query(
                CacheMovieMostPopularEntry.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            // I only want the first cursor, which is the highest rate pop movie.
            cursor.moveToFirst();

            String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
            String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
            String movie_poster_image_thumbnail =
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
            String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
            String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
            String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
            String id = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));

            movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                    , a_plot_synopsis, user_rating, release_date, id);

            cursor.close();
        }

        return movie;
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_notification_large_icon);
        return largeIcon;
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                HIGHEST_RATE_POP_MOVIE_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Action openDetailActivityAction(Context context, Movie movie) {
        Intent startActivityIntent = new Intent(context, DetailActivity.class);
        startActivityIntent.putExtra("movie", movie);
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(startActivityIntent)
                .getPendingIntent(ACTION_GO_TO_PAGE_PENDING_INTENT_ID, PendingIntent.FLAG_UPDATE_CURRENT);
        Action openDetailActivityAction = new Action(R.drawable.ic_notification_large_icon,
                context.getString(R.string.detail_page_notification_action),
                pendingIntent);
        return openDetailActivityAction;
    }

    private static Action ignoreNotificationAction(Context context) {
        Intent ignoreNotificationIntent = new Intent(context, NotificationIntentService.class);
        ignoreNotificationIntent.setAction(NotificationTasks.ACTION_DISMISS_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action ignoreNotificationAction = new Action(R.drawable.ic_cancel_black_24dp,
                context.getString(R.string.ignore_notification_action),
                pendingIntent);
        return ignoreNotificationAction;
    }
}
