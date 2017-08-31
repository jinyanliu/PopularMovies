package se.sugarest.jane.popularmovies.ui;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;
import se.sugarest.jane.popularmovies.jobscheduler.JobSchedulersConstraints;
import se.sugarest.jane.popularmovies.jobscheduler.PersistFavMovie;
import se.sugarest.jane.popularmovies.jobscheduler.PersistPopMovieTask;
import se.sugarest.jane.popularmovies.jobscheduler.PersistTopMovieTask;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.DeleteExtraMoviePicService;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.NotificationService;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.PersistFavService;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.PersistPopService;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.PersistTopService;
import se.sugarest.jane.popularmovies.jobscheduler.jobservice.UpdateWidgetService;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.movie.MovieAdapter;
import se.sugarest.jane.popularmovies.movie.MovieAdapter.MovieAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.notification.lowerversion.ScheduleNotificationLowerVersion;
import se.sugarest.jane.popularmovies.tasks.FetchMoviePostersTask;
import se.sugarest.jane.popularmovies.utilities.DeleteExternalFolderExtraPic;

import static se.sugarest.jane.popularmovies.widget.WidgetConstraints.IntentExtraWidgetTileCode.FAVORITE_PIC_TITLE_CODE;
import static se.sugarest.jane.popularmovies.widget.WidgetConstraints.IntentExtraWidgetTileCode.POPULAR_PIC_TITLE_CODE;
import static se.sugarest.jane.popularmovies.widget.WidgetConstraints.IntentExtraWidgetTileCode.TOPRATED_PIC_TITLE_CODE;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler
        , android.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ACTION_DATA_UPDATED = "se.sugarest.jane.popularmovies.ACTION_DATA_UPDATED";

    private static final String TAG = MainActivity.class.getSimpleName();

    public static Toast mToast;

    public static Boolean mShowToast = false;

    public static final int MOVIE_LOADER = 0;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    public MovieAdapter getmMovieAdapter() {
        return mMovieAdapter;
    }

    public ProgressBar getmLoadingIndicator() {
        return mLoadingIndicator;
    }

    public SwipeRefreshLayout getmSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public Toast getmToast() {
        return mToast;
    }

    public void setmToast(Toast mToast) {
        this.mToast = mToast;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String titleOrderBy = getOrderByPreference();
        if ("popular".equals(titleOrderBy)) {
            setTitle(getString(R.string.main_activity_title_most_popular));
        } else if ("top_rated".equals(titleOrderBy)) {
            setTitle(getString(R.string.main_activity_title_top_rated));
        } else {
            setTitle(getString(R.string.main_activity_title_favorite));
        }

        /**
         * Using findViewById, get a reference to the RecyclerView from xml.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movieposters);

        /**
         * This TextView is used to display errors and will be hidden if there are no errors.
         */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        /**
         * this: Current context, will be used to access resources.
         * 4: The number of columns in the grid
         * GridLayoutManager.VERTICAL: Layout orientation.
         * false: When set to true, layouts from end to start.
         */
        final GridLayoutManager layoutManager
                = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /**
         * Use this setting to improve performance that changes in content do not change the child
         * layout size in the RecyclerView.
         */
        mRecyclerView.setHasFixedSize(true);

        /**
         * The MovieAdapter is responsible for linking the movie posters data with the Views that
         * will end up displaying the posters data.
         */
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(this, this);
        }

        /**
         * Setting the adapter attaches it to the RecyclerView in the layout.
         */
        mRecyclerView.setAdapter(mMovieAdapter);

        /**
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        refreshMovie();
                    }
                }
        );

        // Change swipeRefreshLayout 's loading indicator background color.
        int swipeRefreshBgColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(swipeRefreshBgColor);

        // Change swipeRefreshLayout 's loading indicator loading circle color.
        // You can have as many as colors you want.
        mSwipeRefreshLayout.setColorSchemeResources(
                // if the loading is fast, it shows white from the beginning and finish
                R.color.colorWhiteFavoriteStar,
                // if the loading is slow, it shows different blue
                R.color.trailer10,
                R.color.trailer9,
                R.color.trailer8,
                R.color.trailer7,
                R.color.trailer6,
                R.color.trailer5,
                R.color.trailer4,
                R.color.trailer3,
                R.color.trailer2,
                R.color.trailer1,
                R.color.trailer0);


        // If there is a network connection, fetch data
        if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
            // First loading the app, don't show up to date toast if it is up to date.
            mShowToast = false;

            String orderBy = getOrderByPreference();
            if ("popular".equals(orderBy)) {
                orderBy = "movie/" + orderBy;
                new FetchMoviePostersTask(this).execute(orderBy);
                new PersistPopMovieTask(this).execute();
                initCursorLoader();
            } else if ("top_rated".equals(orderBy)) {
                orderBy = "movie/" + orderBy;
                new FetchMoviePostersTask(this).execute(orderBy);
                new PersistTopMovieTask(this).execute();
                initCursorLoader();
            } else {
                initCursorLoader();
                PersistFavMovie.persistFavMovie(this);
            }

            DeleteExternalFolderExtraPic.deleteExtraMoviePosterFilePic(this);
            DeleteExternalFolderExtraPic.deleteExtraMovieThumbnailFilePic(this);

        } else {
            Boolean enableOffline = getEnableOfflinePreference();
            if (enableOffline) {
                hideLoadingIndicators();
                initCursorLoader();
            } else {
                hideLoadingIndicators();
                showErrorMessage();
                mErrorMessageDisplay.setText(getString(R.string.error_message_enable_offline_false));
            }
        }

        // API 24 Android 7.0 Nougat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "jag Set up 6 job schedulers !");
            scheduleUpdatePopMovieJob();
            scheduleUpdateTopMovieJob();
            scheduleUpdateFavMovieJob();
            scheduleDeleteExtraPic();
            scheduleUpdateWidgetJob();
            scheduleNotificationJob();
        } else {
            ScheduleNotificationLowerVersion.scheduleNotification(this);
        }

        // Just for test
        // NotificationTasks.executeTask(this, NotificationTasks.ACTION_NOTIFY);
    }

    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleUpdatePopMovieJob() {
        Log.i(TAG, "Scheduling fetch pop movie job.");
        ComponentName serviceName = new ComponentName(this, PersistPopService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_PERSIST_POP_MOVIE, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_FETCH_POP_MOVIE, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Fetch pop movie job scheduled successfully!");
        }
    }


    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleUpdateTopMovieJob() {
        Log.i(TAG, "Scheduling fetch top movie job.");
        ComponentName serviceName = new ComponentName(this, PersistTopService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_PERSIST_TOP_MOVIE, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_FETCH_TOP_MOVIE, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Fetch top movie job scheduled successfully!");
        }
    }

    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleUpdateFavMovieJob() {
        Log.i(TAG, "Scheduling fetch fav movie job.");
        ComponentName serviceName = new ComponentName(this, PersistFavService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_PERSIST_FAV_MOVIE, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_FETCH_FAV_MOVIE, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Fetch fav movie job scheduled successfully!");
        }
    }

    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleDeleteExtraPic() {
        Log.i(TAG, "Scheduling delete extra pic job.");
        ComponentName serviceName = new ComponentName(this, DeleteExtraMoviePicService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_DELETE_EXTRA_PIC, serviceName)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_DELETE_EXTRA_PIC, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Scheduler delete extra pic job scheduled successfully!");
        }
    }

    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleUpdateWidgetJob() {
        Log.i(TAG, "Scheduling update widget job.");
        ComponentName serviceName = new ComponentName(this, UpdateWidgetService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_UPDATE_WIDGET, serviceName)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_UPDATE_WIDGET, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Scheduler update widget job scheduled successfully!");
        }
    }

    // N == api 24
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleNotificationJob() {
        Log.i(TAG, "Scheduling notification job.");
        ComponentName serviceName = new ComponentName(this, NotificationService.class);
        JobInfo jobInfo = new JobInfo.Builder(JobSchedulersConstraints.JOB_ID_NOTIFICATION, serviceName)
                .setPeriodic(JobSchedulersConstraints.PERIOD_MILLIS_NOTIFICATION, JobInfo.getMinFlexMillis())
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Scheduler notification scheduled successfully!");
        }
    }

    private void refreshMovie() {
        if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
            mShowToast = true;
            String orderBy = getOrderByPreference();
            if ("popular".equals(orderBy)) {
                orderBy = "movie/" + orderBy;
                new FetchMoviePostersTask(this).execute(orderBy);
                new PersistPopMovieTask(this).execute();
                initCursorLoader();
            } else if ("top_rated".equals(orderBy)) {
                orderBy = "movie/" + orderBy;
                new FetchMoviePostersTask(this).execute(orderBy);
                new PersistTopMovieTask(this).execute();
                initCursorLoader();
            } else {
                initCursorLoader();
                PersistFavMovie.persistFavMovie(this);
            }

            DeleteExternalFolderExtraPic.deleteExtraMoviePosterFilePic(this);
            DeleteExternalFolderExtraPic.deleteExtraMovieThumbnailFilePic(this);

        } else {
            hideLoadingIndicators();
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(MainActivity.this, getString(R.string.toast_message_refresh_no_internet), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        }
    }

    public void initCursorLoader() {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        int titleCode;
        String orderBy = getOrderByPreference();
        if ("popular".equals(orderBy)) {
            titleCode = POPULAR_PIC_TITLE_CODE;
        } else if ("top_rated".equals(orderBy)) {
            titleCode = TOPRATED_PIC_TITLE_CODE;
        } else {
            titleCode = FAVORITE_PIC_TITLE_CODE;
        }
        dataUpdatedIntent.putExtra("title_code", titleCode);
        this.sendBroadcast(dataUpdatedIntent);
        Log.i(TAG, "jag ! jag init loader !");
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    /**
     * This method is overridden by the MainActivity class in order to handle RecyclerView item
     * clicks.
     */
    @Override
    public void onClick(Movie movie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", movie);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showMovieDataView() {
        // First, make sure the error is invisible.
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the movie data is visible.
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie data View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showErrorMessage() {
        // First, hide the currently visible data.
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Then, show the error.
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteAll = menu.findItem(R.id.action_delete_all);
        String orderBy = getOrderByPreference();
        Boolean enableOffline = getEnableOfflinePreference();
        if ("favorites".equals(orderBy)) {
            if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {
                deleteAll.setVisible(true);
            } else {
                if (enableOffline == true) {
                    deleteAll.setVisible(true);
                } else {
                    deleteAll.setVisible(false);
                }
            }
        } else {
            deleteAll.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Have a refresh menu button to perform the refresh again, in case some device or some
            // users cannot perform swipe to refresh.
            case R.id.action_refresh:
                mLoadingIndicator.setVisibility(View.VISIBLE);
                refreshMovie();
                return true;
            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllMovies() {

        Cursor cursor = getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor.getCount() == 0 || cursor == null) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, getString(R.string.delete_all_movie_no_movies_to_delete), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        } else {
            int rowsDeletedFavMovie = getContentResolver().delete(FavMovieEntry.CONTENT_URI, null, null);
            int rowsDeletedReview = getContentResolver().delete(ReviewEntry.CONTENT_URI, null, null);
            int rowsDeletedTrailer = getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);

            if (rowsDeletedFavMovie == 0) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, getString(R.string.delete_all_movie_failed), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            } else {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, getString(R.string.delete_all_movie_successful), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            }
        }
        cursor.close();

        // After user deleted/unliked all movies, they won't trigger initLoader, but the widget
        // should change, so we need to send a broadcase here after deleting all.
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        this.sendBroadcast(dataUpdatedIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String orderBy = getOrderByPreference();

        if ("popular".equals(orderBy)) {
            return new CursorLoader(
                    this,
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else if ("top_rated".equals(orderBy)) {
            return new CursorLoader(
                    this,
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else {
            return new CursorLoader(
                    this,
                    FavMovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            hideLoadingIndicators();
            showMovieDataView();
            mMovieAdapter.swapCursor(cursor);

        } else {

            supportStartPostponedEnterTransition();
            hideLoadingIndicators();
            showErrorMessage();

            String orderBy = getOrderByPreference();

            if ("popular".equals(orderBy)) {
                // Because we only fetch data when there is network, if there is no network, we load
                // movie data from database. so this message will only happen when users open the app
                // for the first time without network (no data in database).
                // We will display "Please check your network connection."
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_popular_movie));
            } else if ("top_rated".equals(orderBy)) {
                // Because we only fetch data when there is network, if there is no network, we load
                // movie data from database. so this message will only happen when users open the app
                // for the first time without network (no data in database).
                // We will display "Please check your network connection."
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_top_rated_movie));
            } else {
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_fav_movie));
            }
        }
    }

    private void hideLoadingIndicators() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // this setRefreshing method is controlling the visible or invisible of the loading
        // indicator of the swipeRefreshlayout
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    public void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @NonNull
    private String getOrderByPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }

    /**
     * Prompt the user to confirm that they want to delete all movies.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all movies.
                deleteAllMovies();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean getEnableOfflinePreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getBoolean(
                getString(R.string.pref_enable_offline_key),
                getResources().getBoolean(R.bool.pref_enable_offline_default));
    }
}
