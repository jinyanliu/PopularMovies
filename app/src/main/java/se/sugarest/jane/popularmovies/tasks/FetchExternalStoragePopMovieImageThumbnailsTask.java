package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;

/**
 * Created by jane on 17-6-20.
 */

public class FetchExternalStoragePopMovieImageThumbnailsTask extends AsyncTask<MovieBasicInfo, Void, String> {

    private MainActivity mainActivity;

    public FetchExternalStoragePopMovieImageThumbnailsTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private static final String TAG = FetchExternalStoragePopMovieImageThumbnailsTask.class.getSimpleName();

    String urlToBeDownloaded;

    String movieId;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(MovieBasicInfo... params) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (params.length == 0) {
            return null;
        }

        urlToBeDownloaded = params[0].getmExternalUrl();

        movieId = params[0].getmId();

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
            Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_filename) + filename);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                    + "/popthumbnails/" + filename);

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
                Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_downloadedSize)
                        + downloadedSize + this.mainActivity.getString(R.string.log_information_message_download_totalSize)
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
        Log.i(TAG, this.mainActivity.getString(R.string.log_information_message_download_filepath) + filepath);
        return filepath;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CacheMovieMostPopularEntry.COLUMN_EXTERNAL_STORAGE_IMAGE_THUMBNAIL, s);
            String selection = CacheMovieMostPopularEntry.COLUMN_MOVIE_ID;
            selection = selection + " =?";
            String[] selectionArgs = {movieId};
            Log.i(TAG, "This is movie Id: " + movieId + ", image thumbnail external path: " + s);
            int rowsUpdated = this.mainActivity.getContentResolver()
                    .update(CacheMovieMostPopularEntry.CONTENT_URI,
                            contentValues,
                            selection,
                            selectionArgs);

            if (rowsUpdated > 0) {
                Log.i(TAG, "Insert external image thumbnail poster path into cache popular movie table successful.");
            }
        } else {
            Log.e(TAG, mainActivity.getString(R.string.log_error_message_offline_before_download_pics_finish));
            String expectedMsg = mainActivity.getString(R.string.toast_message_offline_before_download_finish);

            if (this.mainActivity.getmToast() != null) {
                String displayedText = ((TextView) ((LinearLayout) this.mainActivity.getmToast().getView())
                        .getChildAt(0)).getText().toString();
                if (!displayedText.equals(expectedMsg)) {
                    this.mainActivity.getmToast().cancel();
                    Toast newToast = Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_message_offline_before_download_finish), Toast.LENGTH_SHORT);
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
    }
}
