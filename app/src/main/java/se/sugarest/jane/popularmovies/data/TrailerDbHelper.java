package se.sugarest.jane.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Created by jane on 17-4-11.
 */

/**
 * Manages a local database for trailer data
 */
public class TrailerDbHelper extends SQLiteOpenHelper {

    /**
     * This is the name of the database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "trailer.db";

    /**
     * If changing the database schema, must increment the database version or the onUpgrade method
     * will be called.
     */
    private static final int DATABASE_VERSION = 1;

    public TrailerDbHelper(Context context) {
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
                        TrailerEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                        TrailerEntry.COLUMN_KEY_OF_TRAILER + " TEXT NOT NULL);";

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
