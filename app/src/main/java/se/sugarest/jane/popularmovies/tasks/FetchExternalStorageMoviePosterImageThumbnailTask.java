package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;

/**
 * Created by jane on 17-5-31.
 */

public class FetchExternalStorageMoviePosterImageThumbnailTask extends AsyncTask<String, Void, String> {

    MainActivity mainActivity;

    public FetchExternalStorageMoviePosterImageThumbnailTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private static final String TAG = FetchExternalStorageMoviePosterImageThumbnailTask.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";

    String urlToBeDownloaded;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
        // If there's no image url to download, there's no way of writing image url into sd card and read it out.
        if (params.length == 0) {
            return null;
        }
        urlToBeDownloaded = params[0];
        String fullUrlToBeDownLoaded = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                .concat(urlToBeDownloaded);
        try {


            URL url = new URL(fullUrlToBeDownLoaded);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            // File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();


            // Make sure the Pictures directory exists.
            // SDCardRoot.mkdir();

            String filename = fullUrlToBeDownLoaded;
            Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_poster_thumbnail_filename) + filename);
            // File file = new File(Environment.getExternalStorageDirectory().toString(), filename);

            // File file = new File(SDCardRoot, filename);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + filename);

            // file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            //file.createNewFile();

//            if (file.createNewFile()) {
//                file.createNewFile();
//            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_poster_thumbnail_downloadedSize)
                        + downloadedSize + this.mainActivity.getString(R.string.log_information_message_download_poster_thumbnail_totalSize)
                        + totalSize);
            }
            fileOutput.close();
            if (downloadedSize == totalSize) {
                filepath = file.getPath();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            filepath = null;
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_poster_thumbnail_filepath) + filepath);
        return filepath;
    }

    @Override
    protected void onPostExecute(String s) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
        String orderBy = sharedPrefs.getString(
                this.mainActivity.getString(R.string.settings_order_by_key),
                this.mainActivity.getString(R.string.settings_order_by_default)
        );

        if ("popular".equals(orderBy)) {
            String selection = CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL;
            String[] selectionArgs = {urlToBeDownloaded};

            Cursor cursor = this.mainActivity.getContentResolver()
                    .query(CacheMovieMostPopularEntry.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
                            null);

            cursor.moveToFirst();

            ContentValues values = new ContentValues();
            values.put(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS)));
            values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID)));
            values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, s);
            values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE)));
            values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH)));
            values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE)));
            values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING,
                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING)));
            this.mainActivity.getContentResolver().update(CacheMovieMostPopularEntry.CONTENT_URI, values, selection, selectionArgs);

            cursor.close();

        } else {
            String selection = CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL;
            String[] selectionArgs = {urlToBeDownloaded};

            Cursor cursor = this.mainActivity.getContentResolver()
                    .query(CacheMovieTopRatedEntry.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
                            null);

            cursor.moveToFirst();

            ContentValues values = new ContentValues();
            values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS)));
            values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID)));
            values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, s);
            values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE)));
            values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH)));
            values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE)));
            values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING,
                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_USER_RATING)));
            this.mainActivity.getContentResolver().update(CacheMovieTopRatedEntry.CONTENT_URI, values, selection, selectionArgs);

            cursor.close();
        }
    }
}
