package se.sugarest.jane.popularmovies.jobscheduler;

/**
 * Created by jane on 17-8-31.
 */

public class JobSchedulersConstants {
    /************************************************************************************************
     * Keep in mind ! For test or for real, job scheduler minimum periodic interval is 15 minutes ! *
     ************************************************************************************************/
    /* Interval for the fetch movie periodic job, in milliseconds. */
    public static final long PERIOD_MILLIS_FETCH_POP_MOVIE = 24 * 60 * 60 * 1000L; // 24 * 60 minutes
    public static final long PERIOD_MILLIS_FETCH_TOP_MOVIE = 24 * 60 * 60 * 1000L;
    public static final long PERIOD_MILLIS_FETCH_FAV_MOVIE = 24 * 60 * 60 * 1000L;
    /* Interval for the delete extra pic periodic job, in milliseconds. */
    public static final long PERIOD_MILLIS_DELETE_EXTRA_PIC = 24 * 60 * 60 * 1000L; // 24 * 60 minutes
    /* Interval for the update widget periodic job, in milliseconds. */
    public static final long PERIOD_MILLIS_UPDATE_WIDGET = 24 * 60 * 60 * 1000L; // 24 * 60 minutes
    /* Interval for the notification periodic job, in milliseconds. */
    public static final long PERIOD_MILLIS_NOTIFICATION = 24 * 60 * 60 * 1000L; // 24 * 60 minutes
    public static final int JOB_ID_PERSIST_POP_MOVIE = 111;
    public static final int JOB_ID_PERSIST_TOP_MOVIE = 222;
    public static final int JOB_ID_PERSIST_FAV_MOVIE = 333;
    public static final int JOB_ID_DELETE_EXTRA_PIC = 444;
    public static final int JOB_ID_UPDATE_WIDGET = 555;
    public static final int JOB_ID_NOTIFICATION = 666;
}
