package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
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

                    new FetchExternalStoragePopMovieImageThumbnailsTask(this.mainActivity).execute(
                            new MovieBasicInfo(movieId, fullImageThumbnailForOneMovie));

                    values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());

                    values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());

                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());

                    values.put(CacheMovieMostPopularEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, fullMoviePosterForOneMovie);

                    new FetchExternalStoragePopMoviePosterImagesTask(this.mainActivity).execute(
                            new MovieBasicInfo(movieId, fullMoviePosterForOneMovie));

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

                // After new movie data is fetched from internet, check, loop through the pics folder, only
                // delete those pics which are outdated, not in the new list, so the pics folder can be more
                // stable(movie update is not very quick and frequent, we don't need to delete everything
                // from the very beginning and start fresh). And in MovieAdapter and DetailActivity, we use
                // the external path directly to get the image file paths, even the background task hasn't
                // completely when refresh, we can still get most of the movie poster and thumbnail show up
                // when offline.
                deleteExtraPopMoviePosterFilePic();
                deleteExtraPopMovieImageThumbnailFilePic();

            } else {

                // When latest movie data fetches, delete CacheMovieTopRatedTable
                this.mainActivity.getContentResolver().delete(
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

                    String fullImageThumbnailForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(movieData.get(i).getMoviePosterImageThumbnail());

                    values.put(CacheMovieTopRatedEntry.COLUMN_EXTERNAL_STORAGE_IMAGE_THUMBNAIL, fullImageThumbnailForOneMovie);

                    new FetchExternalStorageTopMovieImageThumbnailsTask(this.mainActivity).execute(
                            new MovieBasicInfo(movieId, fullImageThumbnailForOneMovie));


                    values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());

                    values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());

                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());

                    values.put(CacheMovieTopRatedEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, fullMoviePosterForOneMovie);

                    new FetchExternalStorageTopMoviePosterImagesTask(this.mainActivity).execute(
                            new MovieBasicInfo(movieId, fullMoviePosterForOneMovie));

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

                deleteExtraTopMoviePosterFilePic();
                deleteExtraTopMovieImageThumbnailFilePic();
            }
        } else {
            Log.e(TAG, mainActivity.getString(R.string.log_error_message_offline_before_fetch_movie_data_finish));
            String expectedMsg = mainActivity.getString(R.string.toast_message_offline_before_fetch_movie_data_finish);

            if (this.mainActivity.getmToast() != null) {
                String displayedText = ((TextView) ((LinearLayout) this.mainActivity.getmToast().getView())
                        .getChildAt(0)).getText().toString();
                if (!displayedText.equals(expectedMsg)) {
                    this.mainActivity.getmToast().cancel();
                    Toast newToast = Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_message_offline_before_fetch_movie_data_finish), Toast.LENGTH_SHORT);
                    this.mainActivity.setmToast(newToast);
                    this.mainActivity.getmToast().setGravity(Gravity.BOTTOM, 0, 0);
                    this.mainActivity.getmToast().show();
                }
            } else {
                Toast newToast = Toast.makeText(mainActivity, expectedMsg, Toast.LENGTH_SHORT);
                this.mainActivity.setmToast(newToast);
                this.mainActivity.getmToast().setGravity(Gravity.BOTTOM, 0, 0);
                this.mainActivity.getmToast().show();
            }
        }

        this.mainActivity.initCursorLoader();

    }

    private void deleteExtraPopMoviePosterFilePic() {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
        Cursor cursor = mainActivity.getContentResolver().query(
                CacheMovieMostPopularEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] posterPathArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            posterPathArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
            Log.i(TAG, "filepath: pop poster path in database: " + posterPathArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File popularMoviePicsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/popularmovies/");
        if (popularMoviePicsFolder.exists()) {
            for (File pic : popularMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(posterPathArray).contains(fileName)) {
                    Log.i(TAG, "filepath:delete pop external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void deleteExtraPopMovieImageThumbnailFilePic() {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = mainActivity.getContentResolver().query(
                CacheMovieMostPopularEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] imageThumbnailArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            imageThumbnailArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
            Log.i(TAG, "filepath: pop image thumbnail path in database: " + imageThumbnailArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File popularMovieThumbnailImagesFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/popthumbnails/");
        if (popularMovieThumbnailImagesFolder.exists()) {
            for (File pic : popularMovieThumbnailImagesFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(imageThumbnailArray).contains(fileName)) {
                    Log.i(TAG, "filepath:delete pop external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void deleteExtraTopMoviePosterFilePic() {
        String[] projection = {CacheMovieTopRatedEntry.COLUMN_POSTER_PATH};
        Cursor cursor = mainActivity.getContentResolver().query(
                CacheMovieTopRatedEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] posterPathArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            posterPathArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
            Log.i(TAG, "filepath: top poster path in database: " + posterPathArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File topRatedMoviePicsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/topratedmovies/");
        if (topRatedMoviePicsFolder.exists()) {
            for (File pic : topRatedMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(posterPathArray).contains(fileName)) {
                    Log.i(TAG, "filepath:delete top external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void deleteExtraTopMovieImageThumbnailFilePic() {
        String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = mainActivity.getContentResolver().query(
                CacheMovieTopRatedEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] imageThumbnailArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            imageThumbnailArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
            Log.i(TAG, "filepath: top image thumbnail path in database: " + imageThumbnailArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File topRatedMovieThumbnailImagesFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/topthumbnails/");
        if (topRatedMovieThumbnailImagesFolder.exists()) {
            for (File pic : topRatedMovieThumbnailImagesFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(imageThumbnailArray).contains(fileName)) {
                    Log.i(TAG, "filepath:delete top external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
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
