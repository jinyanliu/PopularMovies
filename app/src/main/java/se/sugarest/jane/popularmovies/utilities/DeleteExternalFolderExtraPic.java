package se.sugarest.jane.popularmovies.utilities;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;

/**
 * Created by jane on 17-7-19.
 */

public class DeleteExternalFolderExtraPic {

    private final static String CACHE_POSTERS_FOLDER_NAME = "/cacheposters/";
    private final static String CACHE_THUMBNAILS_FOLDER_NAME = "/cachethumbnails/";

    private static final String TAG = DeleteExternalFolderExtraPic.class.getSimpleName();

    public static void deleteExtraMoviePosterFilePic(Context context) {

        String[] postersPopPathArray = getPostersPopPathArray(context);

        String[] postersTopPathArray = getPostersTopPathArray(context);

        String[] postersFavPathArray = getPostersFavPathArray(context);

        File postersMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + CACHE_POSTERS_FOLDER_NAME);

        if (postersMoviePicsFolder.exists()) {
            for (File pic : postersMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(postersPopPathArray).contains(fileName)
                        && !Arrays.asList(postersTopPathArray).contains(fileName)
                        && !Arrays.asList(postersFavPathArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    public static void deleteExtraMovieThumbnailFilePic(Context context) {

        String[] thumbnailsPopPathArray = getThumbnailsPopPathArray(context);

        String[] thumbnailsTopPathArray = getThumbnailsTopPathArray(context);

        String[] thumbnailsFavPathArray = getThumbnailsFavPathArray(context);

        File thumbnailsMoviePicsFolder
                = new File(ExternalPathUtils.getExternalPathBasicFileName(context)
                + CACHE_THUMBNAILS_FOLDER_NAME);
        if (thumbnailsMoviePicsFolder.exists()) {
            for (File pic : thumbnailsMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(thumbnailsPopPathArray).contains(fileName)
                        && !Arrays.asList(thumbnailsTopPathArray).contains(fileName)
                        && !Arrays.asList(thumbnailsFavPathArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete pop external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private static String[] getThumbnailsFavPathArray(Context context) {
        String[] projection = {FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = context.getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        String[] imageFavThumbnailArray = new String[cursor.getCount()];
        int i = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                imageFavThumbnailArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                Log.i(TAG, "delete / filepath: fav image thumbnail path in database: " + imageFavThumbnailArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return imageFavThumbnailArray;
    }

    private static String[] getThumbnailsTopPathArray(Context context) {
        String[] projection = {CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = context.getContentResolver().query(
                CacheMovieTopRatedEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] imageThumbnailTopArray = new String[cursor.getCount()];
        int i = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                imageThumbnailTopArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                Log.i(TAG, "delete / filepath: top image thumbnail path in database: " + imageThumbnailTopArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return imageThumbnailTopArray;
    }

    private static String[] getThumbnailsPopPathArray(Context context) {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = context.getContentResolver().query(
                CacheMovieMostPopularEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        String[] imageThumbnailPopArray = new String[cursor.getCount()];
        int i = 0;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                imageThumbnailPopArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                Log.i(TAG, "delete / filepath: pop image thumbnail path in database: " + imageThumbnailPopArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return imageThumbnailPopArray;
    }

    private static String[] getPostersFavPathArray(Context context) {
        String[] projection = {FavMovieEntry.COLUMN_POSTER_PATH};
        Cursor cursor = context.getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        String[] posterFavPathArray = new String[cursor.getCount()];
        int i = 0;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                posterFavPathArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                Log.i(TAG, "delete / filepath: fav poster path in database: " + posterFavPathArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return posterFavPathArray;
    }

    private static String[] getPostersTopPathArray(Context context) {
        String[] projection = {CacheMovieTopRatedEntry.COLUMN_POSTER_PATH};
        Cursor cursor = context.getContentResolver().query(
                CacheMovieTopRatedEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        String[] posterTopPathArray = new String[cursor.getCount()];
        int i = 0;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                posterTopPathArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
                Log.i(TAG, "delete / filepath: top poster path in database: " + posterTopPathArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return posterTopPathArray;
    }

    private static String[] getPostersPopPathArray(Context context) {
        String[] projection = {CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
        Cursor cursor = context.getContentResolver().query(
                CacheMovieMostPopularEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        String[] posterPopPathArray = new String[cursor.getCount()];
        int i = 0;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                posterPopPathArray[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
                Log.i(TAG, "delete / filepath: pop poster path in database: " + posterPopPathArray[i]);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return posterPopPathArray;
    }
}
