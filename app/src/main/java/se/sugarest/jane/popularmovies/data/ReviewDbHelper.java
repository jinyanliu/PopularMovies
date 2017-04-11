package se.sugarest.jane.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;

/**
 * Created by jane on 17-4-11.
 */

/**
 * Manages a local database for review data
 */
public class ReviewDbHelper extends SQLiteOpenHelper {

    /**
     * This is the name of the database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "review.db";

    /**
     * If changing the database schema, must increment the database version or the onUpgrade method
     * will be called.
     */
    private static final int DATABASE_VERSION = 1;

    public ReviewDbHelper(Context context) {
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
                        ReviewEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL);";

        /*
         * After spelling out the SQLite table creation statement above, actually execute
         * that SQL with the execSQL method of the SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
