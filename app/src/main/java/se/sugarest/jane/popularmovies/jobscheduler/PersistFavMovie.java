package se.sugarest.jane.popularmovies.jobscheduler;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageFavMovieImageThumbnailsTask;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageFavMoviePosterImagesTask;
import se.sugarest.jane.popularmovies.ui.MainActivity;
import se.sugarest.jane.popularmovies.utilities.ExternalPathUtils;

/**
 * Created by jane on 17-7-14.
 */

public class PersistFavMovie {

    private static final int POSTER_UP_TO_DATE = 111;
    private static final int THUMBNAIL_UP_TO_DATE = 112;

    private static int mPosterUpToDateRecordNumber;
    private static int mThumbnailUpToDateRecordNumber;

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE_W780 = "w780/";
    private static final String IMAGE_SIZE_W185 = "w185/";
    private static final String CACHE_POSTERS_FOLDER_NAME = "/cacheposters/";
    private static final String CACHE_THUMBNAILS_FOLDER_NAME = "/cachethumbnails/";

    private static final String TAG = PersistFavMovie.class.getSimpleName();

    public static void persistFavMovie(Context context) {

        Log.i(TAG, "Halloooooooooo, jag ar pa fav vag.");

        /*
        download pictures for favorite movies.

        First reason: When we save the movie to fav list database, the 2 pics might not be downloaded
        successfully yet.
        And it will stay break, because we don't refresh fav list again(only call from database for
        fav list).
        (What we are doing now is giving fav list the opportunity to refresh the its pictures' external
        url.)

        Second reason (IMPORTANT): Every time we receive new movie data, we delete the cache tables
        (both POP and TOP) and their external folders completely. But one old movie we have saved to
        fav list before is still stay in our fav list, when it wants to fetch its external url, the
        cache movie folders is already cleaned for new data. There is no way to find it.
        We have to create folders for fav list to maintain its own data.
        */

        downloadExtraFavMoviePosterFilePic(context);
        downloadExtraFavMovieImageThumbnailFilePic(context);

        if (MainActivity.mShowToast) {
            if (mPosterUpToDateRecordNumber == POSTER_UP_TO_DATE && mThumbnailUpToDateRecordNumber == THUMBNAIL_UP_TO_DATE) {
                if (MainActivity.mToast != null) {
                    MainActivity.mToast.cancel();
                }
                MainActivity.mToast = Toast.makeText(context, context.getString(R.string.toast_message_refresh_fav_up_to_date), Toast.LENGTH_SHORT);
                MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                MainActivity.mToast.show();
            }
            MainActivity.mShowToast = false;
        }
    }

    public static void downloadExtraFavMoviePosterFilePic(Context context) {

        File postersMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + CACHE_POSTERS_FOLDER_NAME);

        if (postersMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[postersMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav poster file name count in external folder: " + postersMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : postersMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: fav poster file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            int i = 0;
            String[] newDataArray = new String[cursor.getCount()];

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                newDataArray[i] = currentPosterPath;
                i++;
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentPosterPath)) {
                    Log.i(TAG, "download / filepath: download fav external poster pic:" + currentPosterPath);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(currentPosterPath);
                    new FetchExternalStorageFavMoviePosterImagesTask(context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                mPosterUpToDateRecordNumber = POSTER_UP_TO_DATE;
            }
        } else {
            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_POSTER_PATH};
            Cursor cursor = context.getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download fav external poster pic:" + currentPosterPath);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                        .concat(currentPosterPath);
                new FetchExternalStorageFavMoviePosterImagesTask(context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public static void downloadExtraFavMovieImageThumbnailFilePic(Context context) {

        File thumbnailsMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + CACHE_THUMBNAILS_FOLDER_NAME);

        if (thumbnailsMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[thumbnailsMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav image thumbnail file name count in external folder: " + thumbnailsMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : thumbnailsMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: fav image thumbnail file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            int i = 0;
            String[] newDataArray = new String[cursor.getCount()];

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                newDataArray[i] = currentImageThumbnail;
                i++;
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentImageThumbnail)) {
                    Log.i(TAG, "download / filepath: download fav external image thumbnail pic:" + currentImageThumbnail);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(currentImageThumbnail);
                    new FetchExternalStorageFavMovieImageThumbnailsTask(context).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();

            if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                mThumbnailUpToDateRecordNumber = THUMBNAIL_UP_TO_DATE;

            }
        } else {
            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = context.getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download fav external image thumbnail pic:" + currentImageThumbnail);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                        .concat(currentImageThumbnail);
                new FetchExternalStorageFavMovieImageThumbnailsTask(context).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }
}
