package se.sugarest.jane.popularmovies.widget;

/**
 * Created by jane on 17-8-28.
 */

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.UpdateWidgetService;
import se.sugarest.jane.popularmovies.ui.DetailActivity;
import se.sugarest.jane.popularmovies.ui.MainActivity;

import static se.sugarest.jane.popularmovies.widget.WidgetConstants.IntentExtraWidgetTileCode.FAVORITE_PIC_TITLE_CODE;
import static se.sugarest.jane.popularmovies.widget.WidgetConstants.IntentExtraWidgetTileCode.POPULAR_PIC_TITLE_CODE;
import static se.sugarest.jane.popularmovies.widget.WidgetConstants.IntentExtraWidgetTileCode.TOPRATED_PIC_TITLE_CODE;

/**
 * Provider for a scrollable movie detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetProvider extends AppWidgetProvider {

    final static String TAG = DetailWidgetProvider.class.getSimpleName();

    private int titleCode;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

            // Because there is no title pic for R.layout.widget_detail resource file, we need to
            // give it one from the beginning (first time). So check the preference, that's for that
            // when user click the title, it goes to the right page. fav to fav, pop to pop, top to
            // top. (Consistency with the main app).
            String orderBy = getOrderByPreference(context);
            if ("popular".equals(orderBy)) {
                views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_popular);
                views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_pop));
            } else if ("top_rated".equals(orderBy)) {
                views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_toprated);
                views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_top));
            } else {
                views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_favorite);
                views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_fav));
            }

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);
            // has to be 0
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    // When data change, must initLoader first to get info for main app. So we just check
    // MainActivity.ACTION_DATA_UPDATED
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (MainActivity.ACTION_DATA_UPDATED.equals(intent.getAction())
                || UpdateWidgetService.ACTION_DATA_UPDATED.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

            if (intent.hasExtra("title_code")) {
                titleCode = intent.getExtras().getInt("title_code");
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);
                if (titleCode == POPULAR_PIC_TITLE_CODE) {
                    views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_popular);
                    views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_pop));
                } else if (titleCode == TOPRATED_PIC_TITLE_CODE) {
                    views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_toprated);
                    views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_top));
                } else if (titleCode == FAVORITE_PIC_TITLE_CODE) {
                    views.setImageViewResource(R.id.im_widget_title, R.drawable.widgettitle_favorite);
                    views.setContentDescription(R.id.im_widget_title, context.getString(R.string.a11y_widget_title_fav));
                }
                ComponentName thisWidget = new ComponentName(context, DetailWidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.updateAppWidget(thisWidget, views);
            } else {
                Log.i(TAG, "jag : widget provider on receive without titleCoder, not from initCursorLoader" +
                        ", from JobSchedular or delete All Movies.");
            }
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }

    @NonNull
    public String getOrderByPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(
                context.getString(R.string.settings_order_by_key),
                context.getString(R.string.settings_order_by_default));
    }
}
