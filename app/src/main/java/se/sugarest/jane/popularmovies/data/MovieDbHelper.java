package se.sugarest.jane.popularmovies.data;

/**
 * Created by jane on 17-4-11.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Manages a local database for movie data
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    /*
     * This is the name of the database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "movie.db";

    /*
     * If changing the database schema, must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*
         * This String will contain a simple SQL statement that will create a table that will
         * store the movie data.
         */
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        /**
                         * MovieEntry did not explicitly declare a column called "_ID". However,
                         * MovieEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_A_PLOT_SYNOPSIS + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_USER_RATING + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                        /**
                         * To ensure this table can only contain one movie entry per movie, declaring
                         * the movie_id column to bu unique. Also specify "ON CONFLICT REPLACE". This
                         * tells SQLite that if having a movie entry for a certain movie_id and
                         * attempting to insert another movie entry with that movie_id, replacing
                         * the old movie entry.
                         */
                        " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        /*
         * After spelling out the SQLite table creation statement above, actually execute
         * that SQL with the execSQL method of the SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * store the review data.
         */
        final String SQL_CREATE_REVIEW_TABLE =
                "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                        /**
                         * ReviewEntry did not explicitly declare a column called "_ID". However,
                         * ReviewEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ReviewEntry.COLUMN_MOVIE_ID + " TEXT, " +
                        ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +
                        " FOREIGN KEY(" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + "(" + MovieEntry.COLUMN_MOVIE_ID + "));";

        /*
         * After spelling out the SQLite table creation statement above, actually execute
         * that SQL with the execSQL method of the SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * store the trailer data.
         */
        final String SQL_CREATE_TRAILER_TABLE =
                "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                        /**
                         * TrailerEntry did not explicitly declare a column called "_ID". However,
                         * TrailerEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TrailerEntry.COLUMN_MOVIE_ID + " TEXT, " +
                        TrailerEntry.COLUMN_KEY_OF_TRAILER + " TEXT NOT NULL, " +
                        " FOREIGN KEY(" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + "(" + MovieEntry.COLUMN_MOVIE_ID + "));";

        /*
         * After spelling out the SQLite table creation statement above, actually execute
         * that SQL with the execSQL method of the SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
