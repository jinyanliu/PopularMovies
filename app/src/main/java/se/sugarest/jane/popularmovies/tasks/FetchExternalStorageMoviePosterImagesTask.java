package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularPosterEntry;

/**
 * Created by jane on 17-5-31.
 */

public class FetchExternalStorageMoviePosterImagesTask extends AsyncTask<String, Void, String> {

    Context mContext;

    public FetchExternalStorageMoviePosterImagesTask(Context context) {
        mContext = context;
    }

    private static final String TAG = FetchExternalStorageMoviePosterImagesTask.class.getSimpleName();

//    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
//    private final String IMAGE_SIZE_W780 = "w780/";

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

//        String fullUrlToBeDownLoaded = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
//                .concat(urlToBeDownloaded);
        try {


            URL url = new URL(urlToBeDownloaded);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            // File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();


            // Make sure the Pictures directory exists.
            // SDCardRoot.mkdir();

            String[] parts = urlToBeDownloaded.split("/");
            String lastPart = parts[7];

            String filename = lastPart;
            Log.i(TAG, mContext.getString(R.string.log_information_message_download_poster_filename) + filename);
            // File file = new File(Environment.getExternalStorageDirectory().toString(), filename);

            // File file = new File(SDCardRoot, filename);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + filename);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                boolean fileCreated = file.createNewFile();

                Log.i(TAG, "creating new file: " + file.getAbsolutePath() + ", result: " + fileCreated);
            }else {
                Log.i(TAG, "File already exists: " + file.getAbsolutePath());
            }

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.i(TAG, mContext.getString(R.string.log_information_message_download_poster_downloadedSize)
                        + downloadedSize + mContext.getString(R.string.log_information_message_download_poster_totalSize)
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
        Log.i(TAG, mContext.getString(R.string.log_information_message_download_poster_filepath) + filepath);
        return filepath;
    }

    @Override
    protected void onPostExecute(String s) {

        ContentValues values = new ContentValues();

        values.put(CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH, s);

        Uri newUri = mContext.getContentResolver().insert(CacheMovieMostPopularPosterEntry.CONTENT_URI, values);

        Log.i(TAG, "inserting uri: " + values + "result: " + newUri.toString());

//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
//        String orderBy = sharedPrefs.getString(
//                this.mainActivity.getString(R.string.settings_order_by_key),
//                this.mainActivity.getString(R.string.settings_order_by_default)
//        );
//
//        if ("popular".equals(orderBy)) {
//            String selection = CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL;
//            String[] selectionArgs = {urlToBeDownloaded};
//
//            Cursor cursor = this.mainActivity.getContentResolver()
//                    .query(CacheMovieMostPopularEntry.CONTENT_URI,
//                            null,
//                            selection,
//                            selectionArgs,
//                            null);
//
//            cursor.moveToFirst();
//
//            ContentValues values = new ContentValues();
//            values.put(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS)));
//            values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID)));
//            values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, s);
//            values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE)));
//            values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH)));
//            values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE)));
//            values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING)));
//            this.mainActivity.getContentResolver().update(CacheMovieMostPopularEntry.CONTENT_URI, values, selection, selectionArgs);
//
//            cursor.close();
//
//        } else {
//            String selection = CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL;
//            String[] selectionArgs = {urlToBeDownloaded};
//
//            Cursor cursor = this.mainActivity.getContentResolver()
//                    .query(CacheMovieTopRatedEntry.CONTENT_URI,
//                            null,
//                            selection,
//                            selectionArgs,
//                            null);
//
//            cursor.moveToFirst();
//
//            ContentValues values = new ContentValues();
//            values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS)));
//            values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID)));
//            values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, s);
//            values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE)));
//            values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH)));
//            values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE)));
//            values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING,
//                    cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_USER_RATING)));
//            this.mainActivity.getContentResolver().update(CacheMovieTopRatedEntry.CONTENT_URI, values, selection, selectionArgs);
//
//            cursor.close();
//        }
    }
}
