package se.sugarest.jane.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;

/**
 * Created by jane on 17-4-11.
 */

/**
 * {@link ContentProvider} for Movie data.
 */
public class MovieProvider extends ContentProvider {

    public static final String TAG = MovieProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the movie table
     */
    private static final int MOVIES = 100;

    /**
     * URI matcher code for the content URI for a single movie in the movie table
     */
    private static final int MOVIE_ID = 101;

    /**
     * URI matcher code for the content URI for the review table
     */
    private static final int REVIEWS = 200;

    /**
     * URI matcher code for the content URI for a single review in the review table
     */
    private static final int REVIEW_ID = 201;

    /**
     * URI matcher code for the content URI for the trailer table
     */
    private static final int TRAILERS = 300;

    /**
     * URI matcher code for the content URI for a single review in the review table
     */
    private static final int TRAILER_ID = 301;

    /**
     * URI matcher code for the content URI for the cache movie most popular table
     */
    private static final int CACHE_MOVIES_MOST_POPULAR = 400;

    /**
     * URI matcher code for the content URI for the cache movie top rated table
     */
    private static final int CACHE_MOVIES_TOP_RATED = 500;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called form this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://se.sugarest.jane.popularmovies.data/movie" will
        // map to the integer code {@link #MOVIES}. This URI is used to provide access to MULTIPLE
        // rows of the movie table.
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIES);

        // The content URI of the form "content://se.sugarest.jane.popularmovies.data/movie/#" will
        // map to the integer code {@link #MOVIE_ID}. This URI is used to provide access to ONE
        // single row of the movie table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://se.sugarest.jane.popularmovies.data/movie/3" matches, but
        // "content://se.sugarest.jane.popularmovies.data/movie" (without a number at the end)
        // doesn't match.
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);

        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW, REVIEWS);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/#", REVIEW_ID);

        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER, TRAILERS);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/#", TRAILER_ID);

        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_CACHE_MOVIE_MOST_POPULAR, CACHE_MOVIES_MOST_POPULAR);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_CACHE_MOVIE_TOP_RATED, CACHE_MOVIES_TOP_RATED);
    }

    /**
     * Movie Database helper that will provide access to the movie database
     */
    private MovieDbHelper mMovieDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.

        // To access the database, instantiate the subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments,
     * and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mMovieDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // For the MOVIES code, query the movie table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the movie table.
                // Perform database query on movie table.
                cursor = database.query(
                        MovieEntry.TABLE_NAME, // The table to query
                        projection,            // The columns to return
                        selection,             // The columns for the WHERE clause
                        selectionArgs,         // The values for the WHERE clause
                        null,                  // Don't group the rows
                        null,                  // Don't filter by row groups
                        sortOrder);            // The sort order
                break;
            case MOVIE_ID:
                // For the MOVIE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://se.sugarest.jane.popularmovies/movie/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // argument that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the movie table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case REVIEWS:
                selection = selection + "=?";
                cursor = database.query(
                        ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case REVIEW_ID:
                selection = ReviewEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TrailerEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TRAILERS:
                selection = selection + "=?";
                cursor = database.query(
                        TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRAILER_ID:
                selection = TrailerEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TrailerEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CACHE_MOVIES_MOST_POPULAR:
                cursor = database.query(CacheMovieMostPopularEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CACHE_MOVIES_TOP_RATED:
                cursor = database.query(CacheMovieTopRatedEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.query_default_illegal_argument_exception_message) + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS:
                return ReviewEntry.CONTENT_LIST_TYPE;
            case REVIEW_ID:
                return ReviewEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return TrailerEntry.CONTENT_LIST_TYPE;
            case TRAILER_ID:
                return TrailerEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.get_type_default_illegal_argument_exception_message_part_one)
                        + uri + getContext().getString(R.string.get_type_default_illegal_argument_exception_message_part_two) + match);
        }
    }

    /**
     * Insert a movie into the database with the given content values.
     *
     * @return the new content URI for that specific row in the database.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Get writable database
        SQLiteDatabase database = mMovieDbHelper.getWritableDatabase();

        // The new row id
        long id;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // Insert a new movie into the pets database table with the given ContentValues
                id = database.insert(MovieEntry.TABLE_NAME, null, values);
                break;
            case REVIEWS:
                id = database.insert(ReviewEntry.TABLE_NAME, null, values);
                break;
            case TRAILERS:
                id = database.insert(TrailerEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.insert_default_illegal_argument_exception_message) + uri);
        }

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(TAG, getContext().getResources().getString(R.string.insert_fail_log_message) + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the movie content URI
        // uri: content://se.sugarest.jane.popularmovies/movie
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table, return the new URI with the ID appended
        // to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted;

        // Get writable database
        SQLiteDatabase database = mMovieDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // Delete all rows that match the selection and selection args
                selection = selection + "=?";
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                // Delete a single row given by the ID the URI
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                selection = selection + "=?";
                rowsDeleted = database.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW_ID:
                selection = ReviewEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                selection = selection + "=?";
                rowsDeleted = database.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER_ID:
                selection = TrailerEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri_for_deletion) + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given
        // URI has changed.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        // Get writable database
        SQLiteDatabase database = mMovieDbHelper.getWritableDatabase();
        int rowsInserted = 0;
        switch (sUriMatcher.match(uri)) {
            case CACHE_MOVIES_MOST_POPULAR:
                database.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = database.insert(CacheMovieMostPopularEntry.TABLE_NAME, null,
                                value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            case CACHE_MOVIES_TOP_RATED:
                database.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = database.insert(CacheMovieTopRatedEntry.TABLE_NAME, null,
                                value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
