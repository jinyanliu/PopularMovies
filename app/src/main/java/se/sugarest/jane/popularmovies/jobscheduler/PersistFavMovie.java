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

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE_W780 = "w780/";
    private static final String IMAGE_SIZE_W185 = "w185/";

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

        deleteExtraFavMoviePosterFilePic(context);
        deleteExtraFavMovieImageThumbnailFilePic(context);
    }

    public static void downloadExtraFavMoviePosterFilePic(Context context) {

        File favMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + "/favmovies/");

        if (favMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[favMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav poster file name count in external folder: " + favMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : favMoviePicsFolder.listFiles()) {
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

            if (MainActivity.mShowToast) {
                if (Arrays.asList(fileNameArray).containsAll(Arrays.asList(newDataArray))) {
                    if (MainActivity.mToast != null) {
                        MainActivity.mToast.cancel();
                    }
                    MainActivity.mToast = Toast.makeText(context, context.getString(R.string.toast_message_refresh_fav_up_to_date), Toast.LENGTH_SHORT);
                    MainActivity.mToast.setGravity(Gravity.BOTTOM, 0, 0);
                    MainActivity.mToast.show();
                }
                MainActivity.mShowToast = false;
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

        File favMovieThumbnailsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + "/favthumbnails/");

        if (favMovieThumbnailsFolder.exists()) {

            String[] fileNameArray = new String[favMovieThumbnailsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav image thumbnail file name count in external folder: " + favMovieThumbnailsFolder.listFiles().length);
            int j = 0;
            for (File pic : favMovieThumbnailsFolder.listFiles()) {
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

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
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

    public static void deleteExtraFavMoviePosterFilePic(Context context) {
        String[] projection = {FavMovieEntry.COLUMN_POSTER_PATH};
        Cursor cursor = context.getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] posterPathArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            posterPathArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
            Log.i(TAG, "delete / filepath: fav poster path in database: " + posterPathArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File favMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + "/favmovies/");
        if (favMoviePicsFolder.exists()) {
            for (File pic : favMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(posterPathArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete fav external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    public static void deleteExtraFavMovieImageThumbnailFilePic(Context context) {
        String[] projection = {FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = context.getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] imageThumbnailArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            imageThumbnailArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
            Log.i(TAG, "delete / filepath: fav image thumbnail path in database: " + imageThumbnailArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File favMovieThumbnailsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + "/favthumbnails/");
        if (favMovieThumbnailsFolder.exists()) {
            for (File pic : favMovieThumbnailsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(imageThumbnailArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete fav external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }
}
