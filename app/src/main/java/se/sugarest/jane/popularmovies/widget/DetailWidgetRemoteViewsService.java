package se.sugarest.jane.popularmovies.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.movie.Movie;

/**
 * Created by jane on 17-8-28.
 */

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    // these indices must match the projection
    static final int INDEX_MOVIE_AUTO_ID = 0;
    static final int INDEX_MOVIE_POSTER_PATH = 1;
    static final int INDEX_ORIGINAL_TITLE = 2;
    static final int INDEX_MOVIE_POSTER_IMAGE_THUMBNAIL = 3;
    static final int INDEX_A_PLOT_SYNOPSIS = 4;
    static final int INDEX_USER_RATING = 5;
    static final int INDEX_RELEASE_DATE = 6;
    static final int INDEX_MOVIE_ID = 7;

    public DetailWidgetRemoteViewsService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                String orderBy = getOrderByPreference();

                if ("popular".equals(orderBy)) {
                    String[] movieColumns = {
                            CacheMovieMostPopularEntry._ID,
                            CacheMovieMostPopularEntry.COLUMN_POSTER_PATH,
                            CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE,
                            CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL,
                            CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS,
                            CacheMovieMostPopularEntry.COLUMN_USER_RATING,
                            CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE,
                            CacheMovieMostPopularEntry.COLUMN_MOVIE_ID};

                    data = getContentResolver().query(
                            CacheMovieMostPopularEntry.CONTENT_URI,
                            movieColumns,
                            null,
                            null,
                            null);

                } else if ("top_rated".equals(orderBy)) {
                    String[] movieColumns = {
                            CacheMovieTopRatedEntry._ID,
                            CacheMovieTopRatedEntry.COLUMN_POSTER_PATH,
                            CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE,
                            CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL,
                            CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS,
                            CacheMovieTopRatedEntry.COLUMN_USER_RATING,
                            CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE,
                            CacheMovieTopRatedEntry.COLUMN_MOVIE_ID};

                    data = getContentResolver().query(
                            CacheMovieTopRatedEntry.CONTENT_URI,
                            movieColumns,
                            null,
                            null,
                            null);
                }

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                String poster_path = data.getString(INDEX_MOVIE_POSTER_PATH);

                String original_title = data.getString(INDEX_ORIGINAL_TITLE);
                views.setTextViewText(R.id.widget_movit_title, original_title);

                String movie_poster_image_thumbnail = data.getString(INDEX_MOVIE_POSTER_IMAGE_THUMBNAIL);
                String a_plot_synopsis = data.getString(INDEX_A_PLOT_SYNOPSIS);

                String user_rating = data.getString(INDEX_USER_RATING);
                views.setTextViewText(R.id.widget_user_rate, user_rating);

                String release_date = data.getString(INDEX_RELEASE_DATE);
                String id = data.getString(INDEX_MOVIE_ID);


                final Intent fillIntent = new Intent();
                Movie currentMovie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                        , a_plot_synopsis, user_rating, release_date, id);
                fillIntent.putExtra("movie", currentMovie);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(INDEX_MOVIE_AUTO_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

    @NonNull
    public String getOrderByPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }

}
