package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-4-21.
 */
public class PersistMovieTask extends AsyncTask<String, Void, List<Movie>> {

    private static final String TAG = PersistMovieTask.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";

    MainActivity mainActivity;

    public PersistMovieTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Movie> doInBackground(String... params) {

        // If there's no sortBy method, there's no way of showing movies.
        if (params.length == 0) {
            return Collections.emptyList();
        }

        String sortByMethod = params[0];
        URL movieRequestUrl = NetworkUtils.buildUrl(sortByMethod);

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

            String orderBy = getPreference();

            if ("popular".equals(orderBy)) {

                // When latest movie data fetches, delete CacheMovieMostPopularTable
                this.mainActivity.getContentResolver().delete(
                        CacheMovieMostPopularEntry.CONTENT_URI,
                        null,
                        null);

                // When latest movie data fetches, delete External Storage Folder popularmovies
                File popularMoviePicsFolder
                        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + "/popularmovies/");
                if (popularMoviePicsFolder.exists()) {
                    for (File pic : popularMoviePicsFolder.listFiles()) {
                        Log.i(TAG, "remove existing pic: " + pic.getAbsolutePath());
                        pic.delete();
                    }
                }

                File popularMovieThumbnailImagesFolder
                        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + "/popthumbnails/");
                if (popularMovieThumbnailImagesFolder.exists()) {
                    for (File pic : popularMovieThumbnailImagesFolder.listFiles()) {
                        Log.i(TAG, "remove existing pic: " + pic.getAbsolutePath());
                        pic.delete();
                    }
                }

                int count = movieData.size();
                Vector<ContentValues> cVVector = new Vector<ContentValues>(count);
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());

                    String movieId = movieData.get(i).getId();

                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, movieId);
                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());

                    String fullImageThumbnailForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(movieData.get(i).getMoviePosterImageThumbnail());

                    values.put(CacheMovieMostPopularEntry.COLUMN_EXTERNAL_STORAGE_IMAGE_THUMBNAIL, fullImageThumbnailForOneMovie);

                    // PersistMovieTask is only called when has internet. But we also have the situation that before downloading is finished,
                    // user cut off internet. Have to handle the crush gracefully.
                    if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
                        new FetchExternalStoragePopMovieImageThumbnailsTask(this.mainActivity).execute(
                                new MovieBasicInfo(movieId, fullImageThumbnailForOneMovie));
                        Log.i(TAG, "fetch pop movie poster external");
                    }

                    values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());

                    values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());

                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());

                    values.put(CacheMovieMostPopularEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, fullMoviePosterForOneMovie);

                    if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
                        new FetchExternalStoragePopMoviePosterImagesTask(this.mainActivity).execute(
                                new MovieBasicInfo(movieId, fullMoviePosterForOneMovie));
                        Log.i(TAG, "fetch pop movie image thumbnail external");
                    }

                    values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                    cVVector.add(values);
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int bulkInsertRows = this.mainActivity.getContentResolver().bulkInsert(
                            CacheMovieMostPopularEntry.CONTENT_URI,
                            cvArray);
                    if (bulkInsertRows == cVVector.size()) {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular successful.");
                    } else {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular unsuccessful.");
                    }
                }

                Calendar calendar = Calendar.getInstance();
                Date currentTime = calendar.getTime();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(this.mainActivity.getString(R.string.pref_pop_date_key), currentTime.getTime());
                editor.apply();

            } else {

                // When latest movie data fetches, delete CacheMovieTopRatedTable
                this.mainActivity.getContentResolver().delete(
                        CacheMovieTopRatedEntry.CONTENT_URI,
                        null,
                        null);

                // When latest movie data fetches, delete External Storage Folder topratedmovies
                File topRatedMoviePicsFolder
                        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + "/topratedmovies/");
                if (topRatedMoviePicsFolder.exists()) {
                    for (File pic : topRatedMoviePicsFolder.listFiles()) {
                        Log.i(TAG, "remove existing pic: " + pic.getAbsolutePath());
                        pic.delete();
                    }
                }

                File topRatedMovieThumbnailImagesFolder
                        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + "/topthumbnails/");
                if (topRatedMovieThumbnailImagesFolder.exists()) {
                    for (File pic : topRatedMovieThumbnailImagesFolder.listFiles()) {
                        Log.i(TAG, "remove existing pic: " + pic.getAbsolutePath());
                        pic.delete();
                    }
                }

                int count = movieData.size();
                Vector<ContentValues> cVVector = new Vector<ContentValues>(count);
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());

                    String movieId = movieData.get(i).getId();

                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, movieId);
                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());

                    String fullImageThumbnailForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(movieData.get(i).getMoviePosterImageThumbnail());

                    values.put(CacheMovieTopRatedEntry.COLUMN_EXTERNAL_STORAGE_IMAGE_THUMBNAIL, fullImageThumbnailForOneMovie);

                    if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
                        new FetchExternalStorageTopMovieImageThumbnailsTask(this.mainActivity).execute(
                                new MovieBasicInfo(movieId, fullImageThumbnailForOneMovie));
                    }

                    values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());

                    values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());

                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());

                    values.put(CacheMovieTopRatedEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, fullMoviePosterForOneMovie);

                    if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
                        new FetchExternalStorageTopMoviePosterImagesTask(this.mainActivity).execute(
                                new MovieBasicInfo(movieId, fullMoviePosterForOneMovie));
                    }

                    values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                    cVVector.add(values);
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int bulkInsertRows = this.mainActivity.getContentResolver().bulkInsert(
                            CacheMovieTopRatedEntry.CONTENT_URI,
                            cvArray);
                    if (bulkInsertRows == cVVector.size()) {
                        Log.i(TAG, "bulkInsertCacheMovie TopRated successful.");
                    } else {
                        Log.i(TAG, "bulkInsertCacheMovie TopRated unsuccessful.");
                    }
                }

                Calendar calendar = Calendar.getInstance();
                Date currentTime = calendar.getTime();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(this.mainActivity.getString(R.string.pref_top_date_key), currentTime.getTime());
                editor.apply();
            }
        }


        this.mainActivity.initCursorLoader();
    }

    @NonNull
    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
        return sharedPrefs.getString(
                this.mainActivity.getString(R.string.settings_order_by_key),
                this.mainActivity.getString(R.string.settings_order_by_default)
        );
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) this.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }
}
