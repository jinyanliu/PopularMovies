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
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedPosterEntry;

/**
 * Created by jane on 17-6-15.
 */

public class FetchExternalStorageTopMoviePosterImagesTask extends AsyncTask<String, Void, String> {

    Context mContext;

    public FetchExternalStorageTopMoviePosterImagesTask(Context context) {
        mContext = context;
    }

    private static final String TAG = FetchExternalStorageTopMoviePosterImagesTask.class.getSimpleName();

    String urlToBeDownloaded;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (params.length == 0) {
            return null;
        }

        urlToBeDownloaded = params[0];

        try {

            URL url = new URL(urlToBeDownloaded);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            // Nothing special, just want to give every file a different file name, according to their pass-in url string.
            String[] parts = urlToBeDownloaded.split("/");
            String lastPart = parts[7];
            String filename = lastPart;
            Log.i(TAG, mContext.getString(R.string.log_information_message_download_poster_filename) + filename);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                    + "/topratedmovies/" + filename);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                boolean fileCreated = file.createNewFile();
                Log.i(TAG, "creating new file: " + file.getAbsolutePath() + ", result: " + fileCreated);
            } else {
                Log.i(TAG, "File already exists: " + file.getAbsolutePath());
            }

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength;
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
        values.put(CacheMovieTopRatedPosterEntry.COLUMN_POSTER_PATH, s);
        Uri newUri = mContext.getContentResolver().insert(CacheMovieTopRatedPosterEntry.CONTENT_URI, values);
        Log.i(TAG, "inserting uri: " + values + "result: " + newUri.toString());

    }
}
