package se.sugarest.jane.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;

import se.sugarest.jane.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;
import se.sugarest.jane.popularmovies.utilities.MoviejsonUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

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
         * 2: The number of columns or rows in the grid
         * GridLayoutManager.VERTICAL: Layout orientation.
         * false: When set to true, layouts from end to start.
         */
        GridLayoutManager layoutManager
                = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);

//        LinearLayoutManager layoutManager
//                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

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
        String sortByDefault = "popularity";
        new FetchMovieTask().execute(sortByDefault);
    }

    @Override
    public void onClick(String moviePosterIdThatWasClicked) {

    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
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
    private void showErrorMessage() {
        // First, hide the currently visible data.
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Then, show the error.
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no sortBy method, there's no way of showing movies.
            if (params.length == 0) {
                return null;
            }

            String sortByMethod = params[0];
            URL movieRequestUrl = NetworkUtils.buildUrl(sortByMethod);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);
                String[] simpleJsonMovieData = MoviejsonUtils
                        .getSimpleMoviePostersStringsFromJson(MainActivity.this, jsonMovieResponse);
                return simpleJsonMovieData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                mMovieAdapter.setMoviePosterData(movieData);
            } else {
                showErrorMessage();
            }
        }
    }
}
