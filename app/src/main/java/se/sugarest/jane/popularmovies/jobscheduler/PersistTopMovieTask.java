package se.sugarest.jane.popularmovies.jobscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageTopMovieImageThumbnailsTask;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageTopMoviePosterImagesTask;
import se.sugarest.jane.popularmovies.ui.MainActivity;
import se.sugarest.jane.popularmovies.utilities.ExternalPathUtils;
import se.sugarest.jane.popularmovies.utilities.JsonUtils.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-4-21.
 */
public class PersistTopMovieTask extends AsyncTask<Void, Void, List<Movie>> {

    private static final String TAG = PersistTopMovieTask.class.getSimpleName();

    private static final int POSTER_UP_TO_DATE = 333;
    private static final int THUMBNAIL_UP_TO_DATE = 334;

    private static int mPosterUpToDateRecordNumber;
    private static int mThumbnailUpToDateRecordNumber;

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";
    private final String CACHE_POSTERS_FOLDER_NAME = "/cacheposters/";
    private final String CACHE_THUMBNAILS_FOLDER_NAME = "/cachethumbnails/";

    Context context;

    public PersistTopMovieTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Movie> doInBackground(Void... params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa top vag.");

        URL movieRequestUrl = NetworkUtils.buildUrl("movie/" + "top_rated");

        try {
            String jsonMovieResponse = NetworkUtils
                    .getResponseFromHttpUrl(movieRequestUrl);
            List<Movie> simpleJsonMovieData = MovieJsonUtils
                    .extractResultsFromJson(jsonMovieResponse);
            return simpleJsonMovieData;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    protected void onPostExecute(List<Movie> movieData) {

        if (movieData != null) {

            // When latest movie data fetches, delete CacheMovieTopRatedTable
            this.context.getContentResolver().delete(
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    null,
                    null);

            int count = movieData.size();
            Vector<ContentValues> cVVector = new Vector<ContentValues>(count);
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());

                String movieId = movieData.get(i).getId();

                values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, movieId);
                values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());
                values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());
                values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());
                values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                cVVector.add(values);
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                int bulkInsertRows = this.context.getContentResolver().bulkInsert(
                        CacheMovieTopRatedEntry.CONTENT_URI,
                        cvArray);
                if (bulkInsertRows == cVVector.size()) {
                    Log.i(TAG, "bulkInsertCacheMovie TopRated successful.");
                } else {
                    Log.i(TAG, "bulkInsertCacheMovie TopRated unsuccessful.");
                }
            }

            Boolean enableOffline = getEnableOfflinePreference();
            if (enableOffline == true) {
                downloadExtraTopMoviePosterFilePic();
                downloadExtraTopMovieImageThumbnailFilePic();

                if (MainActivity.mShowToast) {
                    if (mPosterUpToDateRecordNumber == POSTER_UP_TO_DATE && mThumbnailUpToDateRecordNumber == THUMBNAIL_UP_TO_DATE) {
                        if (MainActivity.mToast != null) {
                            MainActivity.mToast.cancel();
                        }
                        MainActivity.mToast = Toast.makeText(this.context, this.context.getString(R.string.toast_message_refresh_top_up_to_date), Toast.LENGTH_SHORT);
                        MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        MainActivity.mToast.show();
                    }
                    // When job scheduler refresh automatically mShowToast should be false.
                    MainActivity.mShowToast = false;
                }
            }
        } else {
            Log.e(TAG, context.getString(R.string.log_error_message_offline_before_fetch_movie_data_finish));
            String expectedMsg = context.getString(R.string.toast_message_offline_before_fetch_movie_data_finish);
            if (MainActivity.mToast != null) {
                String displayedText = ((TextView) ((LinearLayout) MainActivity.mToast.getView())
                        .getChildAt(0)).getText().toString();
                if (!displayedText.equals(expectedMsg)) {
                    MainActivity.mToast.cancel();
                    MainActivity.mToast = Toast.makeText(context, context.getString(R.string.toast_message_offline_before_fetch_movie_data_finish), Toast.LENGTH_SHORT);
                    MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                    MainActivity.mToast.show();
                }
            } else {
                MainActivity.mToast = Toast.makeText(context, expectedMsg, Toast.LENGTH_SHORT);
                MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                MainActivity.mToast.show();
            }
        }
    }

    private void downloadExtraTopMoviePosterFilePic() {

        File postersMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + CACHE_POSTERS_FOLDER_NAME);

        if (postersMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[postersMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: top poster file name count in external folder: " + postersMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : postersMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: top poster file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, CacheMovieTopRatedEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            int i = 0;
            String[] newDataArray = new String[cursor.getCount()];

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
                newDataArray[i] = currentPosterPath;
                i++;
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentPosterPath)) {
                    Log.i(TAG, "download / filepath: download top external poster pic:" + currentPosterPath);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(currentPosterPath);
                    new FetchExternalStorageTopMoviePosterImagesTask(this.context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                mPosterUpToDateRecordNumber = POSTER_UP_TO_DATE;
            }

        } else {
            String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, CacheMovieTopRatedEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download top external poster pic:" + currentPosterPath);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                        .concat(currentPosterPath);
                new FetchExternalStorageTopMoviePosterImagesTask(this.context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void downloadExtraTopMovieImageThumbnailFilePic() {

        File thumbnailsMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + CACHE_THUMBNAILS_FOLDER_NAME);

        if (thumbnailsMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[thumbnailsMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: top image thumbnail file name count in external folder: " + thumbnailsMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : thumbnailsMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: top image thumbnail file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            int i = 0;
            String[] newDataArray = new String[cursor.getCount()];

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                newDataArray[i] = currentImageThumbnail;
                i++;
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentImageThumbnail)) {
                    Log.i(TAG, "download / filepath: download top external image thumbnail pic:" + currentImageThumbnail);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(currentImageThumbnail);
                    new FetchExternalStorageTopMovieImageThumbnailsTask(this.context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                mThumbnailUpToDateRecordNumber = THUMBNAIL_UP_TO_DATE;
            }

        } else {
            String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download top external image thumbnail pic:" + currentImageThumbnail);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                        .concat(currentImageThumbnail);
                new FetchExternalStorageTopMovieImageThumbnailsTask(this.context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private boolean getEnableOfflinePreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        return sharedPrefs.getBoolean(
                this.context.getString(R.string.pref_enable_offline_key),
                this.context.getResources().getBoolean(R.bool.pref_enable_offline_default));
    }
}

