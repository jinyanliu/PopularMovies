package se.sugarest.jane.popularmovies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.movie.FullMovie;
import se.sugarest.jane.popularmovies.movie.MovieAdapter;
import se.sugarest.jane.popularmovies.movie.MovieAdapter.MovieAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.tasks.FetchMoviePostersTask;
import se.sugarest.jane.popularmovies.tasks.FetchMovieTask;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler, android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MOVIE_LOADER = 0;

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private Date mPopLatestRefreshed;

    private Date mTopLatestRefreshed;

    public MovieAdapter getmMovieAdapter() {
        return mMovieAdapter;
    }

    public TextView getmErrorMessageDisplay() {
        return mErrorMessageDisplay;
    }

    public void setmMovieAdapter(MovieAdapter mMovieAdapter) {
        this.mMovieAdapter = mMovieAdapter;
    }

    public ProgressBar getmLoadingIndicator() {
        return mLoadingIndicator;
    }

    public void setmLoadingIndicator(ProgressBar mLoadingIndicator) {
        this.mLoadingIndicator = mLoadingIndicator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Using findViewById, get a reference to the RecyclerView from xml.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movieposters);

        /**
         * This TextView is used to display errors and will be hidden if there are no errors.
         */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /**
         * this: Current context, will be used to access resources.
         * 4: The number of columns in the grid
         * GridLayoutManager.VERTICAL: Layout orientation.
         * false: When set to true, layouts from end to start.
         */
        GridLayoutManager layoutManager
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
            /**
             * Setting the adapter attaches it to the RecyclerView in the layout.
             */
            mRecyclerView.setAdapter(mMovieAdapter);
        }

        /**
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // If there is a network connection, fetch data
        if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {

//            Calendar calendar = Calendar.getInstance();
//            Date currentTime = calendar.getTime();
//            calendar.roll(Calendar.DATE, -1);
//            Date oneDayAgoThisTime = calendar.getTime();
//
//            boolean refreshPop = false;
//            boolean refreshTop = false;
//
//            String orderBy = getPreference();
//            if ("popular".equals(orderBy)) {
//                if (mPopLatestRefreshed == null) {
//                    mPopLatestRefreshed = currentTime;
//                    refreshPop = true;
//                } else {
//                    if (mPopLatestRefreshed.before(oneDayAgoThisTime)) {
//                        refreshPop = true;
//                    }
//                }
//            } else {
//                if (mTopLatestRefreshed == null) {
//                    mTopLatestRefreshed = currentTime;
//                    refreshTop = true;
//                } else {
//                    if (mTopLatestRefreshed.before(oneDayAgoThisTime)) {
//                        refreshTop = true;
//                    }
//                }
//            }
//
//            this.mMovieAdapter.setmLoadFromDb(false);
//
//            /**
//             * Because there are lots of background tasks happening in this app, we use these 2 methods
//             * to quickly load pictures first. So users won't wait for the first time they install the
//             * app.
//             */
//            if (refreshPop || refreshTop) {
//                loadMoviePostersDataFromWeb();
//                saveMovieDataToDb();
//
//                if (refreshPop) {
//                    Log.i(TAG, "Refreshed and stored popular movies from net.");
//                    mPopLatestRefreshed = currentTime;
//                }
//                if (refreshTop) {
//                    Log.i(TAG, "Refreshed and stored top-rated movies from net.");
//                    mTopLatestRefreshed = currentTime;
//                }
//            } else {
//                Log.i(TAG, "Refreshed movies from net without storing.");
//                loadMoviePostersDataFromWeb();
//            }

            String orderBy = getPreference();

            if (!"favorites".equals(orderBy)) {
                orderBy = "movie/" + orderBy;
                new FetchMoviePostersTask(this).execute(orderBy);
                new FetchMovieTask(this).execute(orderBy);
            } else {
                initCursorLoader();
            }

        } else {

//            Log.i(TAG, "Loading data from DB.");
//            this.mMovieAdapter.setmLoadFromDb(true);

            initCursorLoader();
        }
    }

    public void initCursorLoader() {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    /**
     * This method is overridden by the MainActivity class in order to handle RecyclerView item
     * clicks.
     */
    @Override
    public void onClick(FullMovie movie) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String orderBy = getPreference();

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
                    MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showMovieDataView();
            mMovieAdapter.swapCursor(cursor);

        } else {

            supportStartPostponedEnterTransition();
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showErrorMessage();

            String orderBy = getPreference();

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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    public void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @NonNull
    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }

}
