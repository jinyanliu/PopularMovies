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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
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
        mMovieAdapter = new MovieAdapter(this, this);

        /**
         * Setting the adapter attaches it to the RecyclerView in the layout.
         */
        mRecyclerView.setAdapter(mMovieAdapter);

        /**
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            /**
             * Because there are lots of background tasks happening in this app, we use these 2 methods
             * to quickly load pictures first. So users won't wait for the first time they install the
             * app. 
             */
            loadMoviePostersData();

            /**
             * Load movie data and store them in the database.
             */
            loadMovieData();
        } else {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // Get a reference to the ConnectivityManager to check state of network connectivity.
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        // Get details on the currently active default data network
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        if (networkInfo != null && networkInfo.isConnected()) {
//            showMovieDataView();
//            /**
//             * Once all of the views are setup, movie data can be load.
//             */
//            loadMovieData();
//        }
//    }

    public void initCursorLoader() {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    private void loadMoviePostersData() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        if (!"favorites".equals(orderBy)) {
            orderBy = "movie/" + orderBy;
            new FetchMoviePostersTask(this).execute(orderBy);
        } else {
            initCursorLoader();
        }

    }

    /**
     * This method will get the user's preferred sortBy method for movies, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData() {

        // showMovieDataView();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        if (!"favorites".equals(orderBy)) {
            orderBy = "movie/" + orderBy;
            new FetchMovieTask(this).execute(orderBy);
        } else {
            initCursorLoader();
        }


//        if ("favorites".equals(orderBy)) {
//            showDatabaseMoviePoster();
//        } else
//        if ("popular".equals(orderBy)) {
//            orderBy = "movie/" + orderBy;
//            new FetchMovieTask(this).execute(orderBy);
//
//            // showDataBaseCacheMovieMostPopularPoster();
//        } else {
//            orderBy = "movie/" + orderBy;
//            new FetchMovieTask(this).execute(orderBy);
//
//            // showDataBaseCacheMovieTopRatedPoster();
//        }
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

//    private void showDatabaseMoviePoster() {
//
//        // Create an empty ArrayList that can start adding movies to
//        List<Movie> movies = new ArrayList<>();
//
//        // Perform a query on the provider using the ContentResolver.
//        // Use the {@link MovieEntry#CONTENT_URI} to access the movie data.
//        Cursor cursor = getContentResolver().query(
//                MovieEntry.CONTENT_URI,    // The content URI of the movie table
//                null,                      // The columns to return for each row
//                null,
//                null,
//                null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//
//            mMovieAdapter.setMoviePosterData(movies);
//        } else {
//            showErrorMessage();
//            mErrorMessageDisplay.setText(getString(R.string.error_message_no_fav_movie));
//        }
//
//        cursor.close();
//    }

//    private void showDataBaseCacheMovieMostPopularPoster() {
//
//        getmLoadingIndicator().setVisibility(View.VISIBLE);
//
//        // Create an empty ArrayList that can start adding movies to
//        List<Movie> movies = new ArrayList<>();
//
//        // Perform a query on the provider using the ContentResolver.
//        // Use the {@link CacheMovieMostPopularEntry#CONTENT_URI} to access the movie data.
//        Cursor cursor = getContentResolver().query(
//                CacheMovieMostPopularEntry.CONTENT_URI, // The content URI of the cache movie most popular table
//                null,                                   // The columns to return for each row
//                null,
//                null,
//                null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//            cursor.close();
//            getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            mMovieAdapter.setMoviePosterData(movies);
//        } else {
//            getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            showErrorMessage();
//            mErrorMessageDisplay.setText(getString(R.string.error_message_no_popular_movie));
//        }
//    }

//    private void showDataBaseCacheMovieTopRatedPoster() {
//
//        getmLoadingIndicator().setVisibility(View.VISIBLE);
//
//        // Create an empty ArrayList that can start adding movies to
//        List<Movie> movies = new ArrayList<>();
//
//        // Perform a query on the provider using the ContentResolver.
//        // Use the {@link CacheMovieTopRatedEntry#CONTENT_URI} to access the movie data.
//        Cursor cursor = getContentResolver().query(
//                CacheMovieTopRatedEntry.CONTENT_URI, // The content URI of the cache movie top rated table
//                null,                                // The columns to return for each row
//                null,
//                null,
//                null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            mMovieAdapter.setMoviePosterData(movies);
//        } else {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            showErrorMessage();
//            mErrorMessageDisplay.setText(getString(R.string.error_message_no_top_rated_movie));
//        }
//
//        cursor.close();
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

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
            mMovieAdapter.swapCursor(cursor);
//        }
//
//
//        mMovieAdapter.swapCursor(cursor);
//        List<Movie> movies = new ArrayList<>();
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            mMovieAdapter.setMoviePosterData(movies);

        } else {

            supportStartPostponedEnterTransition();
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showErrorMessage();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String orderBy = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default));

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
        //cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    public void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }


//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        return new CursorLoader(
//                this,
//                MovieContract.CacheMovieMostPopularEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null);
//    }
//
//    @Override
//    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
//
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        getmMovieAdapter().swapCursor(cursor);
//        List<Movie> movies = new ArrayList<>();
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//            cursor.close();
//            this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            this.mainActivity.getmMovieAdapter().setMoviePosterData(movies);
//        } else {
//            this.mainActivity.supportStartPostponedEnterTransition();
//            this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            this.mainActivity.showErrorMessage();
//            this.mainActivity.getmErrorMessageDisplay().setText(this.mainActivity.getString(R.string.error_message_no_popular_movie));
//        }
//
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//        this.mainActivity.getmMovieAdapter().swapCursor(null);
//    }
}
