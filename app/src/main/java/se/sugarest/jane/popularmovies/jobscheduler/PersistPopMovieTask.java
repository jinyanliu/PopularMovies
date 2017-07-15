package se.sugarest.jane.popularmovies.jobscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
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
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStoragePopMovieImageThumbnailsTask;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStoragePopMoviePosterImagesTask;
import se.sugarest.jane.popularmovies.ui.MainActivity;
import se.sugarest.jane.popularmovies.utilities.ExternalPathUtils;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-4-21.
 */
public class PersistPopMovieTask extends AsyncTask<Void, Void, List<Movie>> {

    private static final String TAG = PersistPopMovieTask.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";

    Context context;

    public PersistPopMovieTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Movie> doInBackground(Void... params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa pop vag.");

        URL movieRequestUrl = NetworkUtils.buildUrl("movie/" + "popular");

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

                // When latest movie data fetches, delete CacheMovieMostPopularTable
                this.context.getContentResolver().delete(
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
                    values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());
                    values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());
                    values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                    cVVector.add(values);
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int bulkInsertRows = this.context.getContentResolver().bulkInsert(
                            CacheMovieMostPopularEntry.CONTENT_URI,
                            cvArray);
                    if (bulkInsertRows == cVVector.size()) {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular successful.");
                    } else {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular unsuccessful.");
                    }
                }

                downloadExtraPopMoviePosterFilePic();
                downloadExtraPopMovieImageThumbnailFilePic();

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

    private void downloadExtraPopMoviePosterFilePic() {

        File popularMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + "/popularmovies/");

        if (popularMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[popularMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: pop poster file name count in external folder: " + popularMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : popularMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: pop poster file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            int i = 0;
            String[] newDataArray = new String[cursor.getCount()];

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
                newDataArray[i] = currentPosterPath;
                i++;
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentPosterPath)) {
                    Log.i(TAG, "download / filepath: download pop external poster pic:" + currentPosterPath);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(currentPosterPath);
                    new FetchExternalStoragePopMoviePosterImagesTask(this.context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (MainActivity.mShowToast) {
                if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                    if (MainActivity.mToast != null) {
                        MainActivity.mToast.cancel();
                    }
                    MainActivity.mToast = Toast.makeText(this.context, this.context.getString(R.string.toast_message_refresh_pop_up_to_date), Toast.LENGTH_SHORT);
                    MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                    MainActivity.mToast.show();
                }
                // When job scheduler refresh automatically mShowToast should be false.
                MainActivity.mShowToast = false;
            }

        } else {
            String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download pop external poster pic:" + currentPosterPath);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                        .concat(currentPosterPath);
                new FetchExternalStoragePopMoviePosterImagesTask(this.context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void downloadExtraPopMovieImageThumbnailFilePic() {

        File popularMovieThumbnailImagesFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + "/popthumbnails/");

        if (popularMovieThumbnailImagesFolder.exists()) {

            String[] fileNameArray = new String[popularMovieThumbnailImagesFolder.listFiles().length];
            Log.i(TAG, "download / filepath: pop image thumbnail file name count in external folder: " + popularMovieThumbnailImagesFolder.listFiles().length);
            int j = 0;
            for (File pic : popularMovieThumbnailImagesFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: pop image thumbnail file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentImageThumbnail)) {
                    Log.i(TAG, "download / filepath: download pop external image thumbnail pic:" + currentImageThumbnail);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(currentImageThumbnail);
                    new FetchExternalStoragePopMovieImageThumbnailsTask(this.context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download pop external image thumbnail pic:" + currentImageThumbnail);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                        .concat(currentImageThumbnail);
                new FetchExternalStoragePopMovieImageThumbnailsTask(this.context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void deleteExtraPopMoviePosterFilePic() {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
        Cursor cursor = context.getContentResolver().query(
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
            Log.i(TAG, "delete / filepath: pop poster path in database: " + posterPathArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File popularMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + "/popularmovies/");
        if (popularMoviePicsFolder.exists()) {
            for (File pic : popularMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(posterPathArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete pop external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void deleteExtraPopMovieImageThumbnailFilePic() {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = context.getContentResolver().query(
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
            Log.i(TAG, "delete / filepath: pop image thumbnail path in database: " + imageThumbnailArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File popularMovieThumbnailImagesFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(this.context)
                + "/popthumbnails/");
        if (popularMovieThumbnailImagesFolder.exists()) {
            for (File pic : popularMovieThumbnailImagesFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(imageThumbnailArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete pop external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }
}
