package se.sugarest.jane.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.movie.MovieAdapter;
import se.sugarest.jane.popularmovies.movie.MovieAdapter.MovieAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.tasks.FetchMovieTask;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    public MovieAdapter getmMovieAdapter() {
        return mMovieAdapter;
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

        /**
         * Once all of the views are setup, movie data can be load.
         */
        loadMovieData();
    }

    /**
     * This method will get the user's preferred sortBy method for movies, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData() {
        showMovieDataView();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        if ("favorites".equals(orderBy)) {
            showDatabaseMoviePoster();
        } else {
            orderBy = "movie/" + orderBy;
            new FetchMovieTask(this).execute(orderBy);
        }
    }

    /**
     * This method is overridden by the MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param currentMovie The current Movie object that was clicked
     */
    @Override
    public void onClick(Movie currentMovie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", currentMovie);
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

    private void showDatabaseMoviePoster() {

        // Create an empty ArrayList that can start adding movies to
        List<Movie> movies = new ArrayList<>();

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link MovieEntry#CONTENT_URI} to access the movie data.
        Cursor cursor = getContentResolver().query(
                MovieEntry.CONTENT_URI,    // The content URI of the movie table
                null,                      // The columns to return for each row
                null,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String poster_path = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
                String original_title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
                String movie_poster_image_thumbnail =
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_A_PLOT_SYNOPSIS));
                String user_rating = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING));
                String release_date = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                String id = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));

                // Create a new {@link Movie} object with the poster_path, original_title,
                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
                // from the cursor response.
                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                        , a_plot_synopsis, user_rating, release_date, id);

                // Add the new {@link Movie} to the list of movies.
                movies.add(movie);
                cursor.moveToNext();
            }
            cursor.close();
            mMovieAdapter.setMoviePosterData(movies);
        } else {
            showErrorMessage();
            mErrorMessageDisplay.setText(getString(R.string.error_message_no_fav_movie));
        }
    }
}
