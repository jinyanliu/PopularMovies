package se.sugarest.jane.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.sugarest.jane.popularmovies.data.MovieContract.MovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;
import se.sugarest.jane.popularmovies.data.MovieDbHelper;
import se.sugarest.jane.popularmovies.databinding.ActivityDetailBinding;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.review.Review;
import se.sugarest.jane.popularmovies.review.ReviewAdapter;
import se.sugarest.jane.popularmovies.trailer.Trailer;
import se.sugarest.jane.popularmovies.trailer.TrailerAdapter;
import se.sugarest.jane.popularmovies.trailer.TrailerAdapter.TrailerAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;
import se.sugarest.jane.popularmovies.utilities.ReviewJsonUtils;
import se.sugarest.jane.popularmovies.utilities.TrailerJsonUtils;

/**
 * Created by jane on 3/1/17.
 */

public class DetailActivity extends AppCompatActivity implements TrailerAdapterOnClickHandler {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final int SAVE_MOVIE_SUCCESS = 10;
    private static final int SAVE_MOVIE_FAIL = 11;
    private static final int SAVE_REVIEW_SUCCESS = 20;
    private static final int SAVE_REVIEW_FAIL = 21;
    private static final int SAVE_TRAILER_SUCCESS = 50;
    private static final int SAVE_TRAILER_FAIL = 51;

    private static final int DELETE_MOVIE_SUCCESS = 30;
    private static final int DELETE_MOVIE_FAIL = 31;
    private static final int DELETE_REVIEW_SUCCESS = 40;
    private static final int DELETE_REVIEW_FAIL = 41;
    private static final int DELETE_TRAILER_SUCCESS = 60;
    private static final int DELETE_TRAILER_FAIL = 61;

    private int saveMovieRecordNumber;
    private int saveReviewRecordNumber;

    private int deleteMovieRecordNumber;
    private int deleteReviewRecordNumber;

    private int saveTrailerRecordNumber;
    private int deleteTrailerRecordNumber;

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W185 = "w185/";
    private final String IMAGE_SIZE_W780 = "w780/";

    private Movie mCurrentMovie;

    private RecyclerView mReviewRecyclerView;

    private RecyclerView mTrailerRecyclerView;

    private ReviewAdapter mReviewAdapter;

    private TrailerAdapter mTrailerAdapter;

    private List<Review> mCurrentMovieReviews;

    private List<Trailer> mCurrentMovieTrailers;

    private String mNumberOfReviewString;

    private String mNumberOfTrailerString;

    /**
     * Movie Database helper that will provide access to the movie database
     */
    private MovieDbHelper mMovieDbHelper;

    /**
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DateBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private ActivityDetailBinding mDetailBinding;

    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mCurrentMovie = (Movie) getIntent().getExtras().getSerializable("movie");
            }
        }

        // Set current movie original title on the detail activity menu bar as activity's title.
        setTitle(mCurrentMovie.getOriginalTitle());

        // Set current movie poster image thumbnail
        String currentMoviePosterImageThumbnail = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780).concat(mCurrentMovie.getMoviePosterImageThumbnail());
        Picasso.with(DetailActivity.this).load(currentMoviePosterImageThumbnail)
                .into(mDetailBinding.ivMoviePosterImageThumbnail);

        // Set current movie textViews content
        mDetailBinding.tvUserRating.setText(mCurrentMovie.getUserRating());
        mDetailBinding.tvReleaseDate.setText(mCurrentMovie.getReleaseDate());
        mDetailBinding.tvAPlotSynopsis.setText(mCurrentMovie.getAPlotSynopsis());

        // To access the database, instantiate the subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mMovieDbHelper = new MovieDbHelper(this);

         /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManagerReviews = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);

        mReviewRecyclerView = mDetailBinding.recyclerviewMovieReviews;

        mReviewRecyclerView.setLayoutManager(layoutManagerReviews);

        /**
         * The ReviewAdapter is responsible for linking the reviews data with the Views that
         * will end up displaying the reviews data.
         */
        mReviewAdapter = new ReviewAdapter();

        /**
         * Setting the adapter attaches it to the Review RecyclerView in the layout.
         */
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        /**
         * Once all of the views are setup, review data can be load.
         */
        loadReviewData(mCurrentMovie.getId());

        LinearLayoutManager layoutManagerTrailers = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);

        mTrailerRecyclerView = mDetailBinding.recyclerviewMovieTrailers;

        mTrailerRecyclerView.setLayoutManager(layoutManagerTrailers);

        /**
         * The TrailerAdapter is responsible for linking the trailers data with the Views that
         * will end up displaying the trailers data.
         */
        mTrailerAdapter = new TrailerAdapter(this, this);

        /**
         * Setting the adapter attaches it to the Trailer RecyclerView in the layout.
         */
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);

        /**
         * Once all of the views are setup, trailer data can be load.
         */
        loadTrailerData(mCurrentMovie.getId());

        // Setup fab_favorite to add favorite movies into database and change FAB color to yellow
        final FloatingActionButton fab_favorite = (FloatingActionButton) findViewById(R.id.fab_favorite);

        fab_favorite.setColorFilter(ContextCompat.getColor(DetailActivity.this, setFabButtonStarColor()));

        fab_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setFabButtonStarColor() == R.color.colorWhiteFavoriteStar) {
                    fab_favorite.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorYellowFavoriteStar));
                    try {
                        saveMovie();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(DetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    fab_favorite.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorWhiteFavoriteStar));
                    deleteMovie();
                }
            }
        });
    }

    /**
     * This method will use the pass in movie id to either tell the background method to get the
     * movie review data in the background or load the review data from the database.
     *
     * @param id The id of the movie clicked.
     */
    private void loadReviewData(String id) {
        try {
            boolean movieIsInDatabase = checkIsMovieAlreadyInFavDatabase(id);
            if (movieIsInDatabase) {
                loadReviewDataFromDatabase(id);
            } else {
                new FetchReviewTask().execute(id);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will use the pass in movie id to either tell the background method to get the
     * movie trailer data in the background or load the trailer data from the database.
     *
     * @param id The id of the movie clicked.
     */
    private void loadTrailerData(String id) {
        new FetchTrailerTask().execute(id);
    }

    /**
     * This method is overridden by the DetailActivity class in order to handle RecyclerView item
     * clicks.
     * <p>
     * Props for supporting the YouTube app if it's available, and falling back to the web browser
     * if necessary.
     *
     * @param trailerSourceKey The current trailerSourceKey that was clicked
     */
    @Override
    public void onClick(String trailerSourceKey) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerSourceKey));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + trailerSourceKey));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    public class FetchReviewTask extends AsyncTask<String, Void, List<Review>> {

        @Override
        protected List<Review> doInBackground(String... params) {
            // If there's no movie id, there's no movie reviews to show.
            if (params.length == 0) {
                return null;
            }
            String id = params[0];
            URL movieReviewRequestUrl = NetworkUtils.buildReviewUrl(id);

            try {
                String jsonMovieReviewResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieReviewRequestUrl);
                List<Review> simpleJsonReviewData = ReviewJsonUtils
                        .extractResultsFromMovieReviewJson(jsonMovieReviewResponse);
                return simpleJsonReviewData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewData) {
            if (reviewData != null) {
                mCurrentMovieReviews = reviewData;
                mReviewAdapter.setReviewData(reviewData);
                // Display total number of reviews in the detail activity, because some movies does
                // not have reviews.
                mNumberOfReviewString = Integer.toString(mReviewAdapter.getItemCount());
                mDetailBinding.tvNumberOfUserReview.setText(mNumberOfReviewString);
            }
        }
    }

    public class FetchTrailerTask extends AsyncTask<String, Void, List<Trailer>> {

        @Override
        protected List<Trailer> doInBackground(String... params) {
            // If there's no movie id, there's no movie trailers to show.
            if (params.length == 0) {
                return null;
            }
            String id = params[0];
            URL movieTrailerRequestUrl = NetworkUtils.buildTrailerUrl(id);

            try {
                String jsonMovieTrailerResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieTrailerRequestUrl);
                List<Trailer> simpleJsonTrailerData = TrailerJsonUtils
                        .extractResultsFromMovieTrailerJson(jsonMovieTrailerResponse);
                return simpleJsonTrailerData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Trailer> trailerData) {
            if (trailerData != null) {
                mCurrentMovieTrailers = trailerData;
                mTrailerAdapter.setReviewData(trailerData);
                mNumberOfTrailerString = Integer.toString(mTrailerAdapter.getItemCount());
            }
        }
    }

    /**
     * Save movie, review and trailer into database.
     */
    private void saveMovie() {
        if (mCurrentMovieReviews.size() > 0) {
            saveFavoriteMovie();
            saveFavoriteTrailer();
            saveFavoriteReview();
            if (mToast != null) {
                mToast.cancel();
            }
            if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS && saveReviewRecordNumber == SAVE_REVIEW_SUCCESS
                    && saveTrailerRecordNumber == SAVE_TRAILER_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                mToast.show();
            }
        } else {
            saveFavoriteMovie();
            saveFavoriteTrailer();
            if (mToast != null) {
                mToast.cancel();
            }
            if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS && saveTrailerRecordNumber == SAVE_TRAILER_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }

    /**
     * Delete movie, review and trailer from database.
     */
    private void deleteMovie() {
        if (mCurrentMovieReviews.size() > 0) {
            deleteFavoriteMovie();
            deleteFavoriteTrailer();
            deleteFavoriteReview();
            if (mToast != null) {
                mToast.cancel();
            }
            if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS && deleteReviewRecordNumber == DELETE_REVIEW_SUCCESS
                    && deleteTrailerRecordNumber == DELETE_TRAILER_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                mToast.show();
            }
        } else {
            deleteFavoriteMovie();
            deleteFavoriteTrailer();
            if (mToast != null) {
                mToast.cancel();
            }
            if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS && deleteTrailerRecordNumber == DELETE_TRAILER_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }

    /**
     * Save movie into database.
     */
    private void saveFavoriteMovie() {
        // Create a ContentValues object where column names are the keys, and current movie
        // attributes are the values.
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_POSTER_PATH, mCurrentMovie.getPosterPath());
        values.put(MovieEntry.COLUMN_ORIGINAL_TITLE, mCurrentMovie.getOriginalTitle());
        values.put(MovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, mCurrentMovie.getMoviePosterImageThumbnail());
        values.put(MovieEntry.COLUMN_A_PLOT_SYNOPSIS, mCurrentMovie.getAPlotSynopsis());
        values.put(MovieEntry.COLUMN_USER_RATING, mCurrentMovie.getUserRating());
        values.put(MovieEntry.COLUMN_RELEASE_DATE, mCurrentMovie.getReleaseDate());
        values.put(MovieEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());

        // Insert a new movie into the provider, returning the content URI for the new movie.
        Uri newUri = getContentResolver().insert(MovieEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            saveMovieRecordNumber = SAVE_MOVIE_FAIL;
            Log.e(TAG, getString(R.string.insert_movie_movie_failed));
        } else {
            saveMovieRecordNumber = SAVE_MOVIE_SUCCESS;
            Log.i(TAG, getString(R.string.insert_movie_movie_successful));
        }
    }

    /**
     * Save review into database.
     */
    private void saveFavoriteReview() {

        for (int i = 0; i < Integer.valueOf(mNumberOfReviewString); i++) {
            ContentValues values = new ContentValues();
            values.put(ReviewEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
            values.put(ReviewEntry.COLUMN_AUTHOR, mCurrentMovieReviews.get(i).getAuthor());
            values.put(ReviewEntry.COLUMN_REVIEW_CONTENT, mCurrentMovieReviews.get(i).getReviewContent());

            Uri newUri = getContentResolver().insert(ReviewEntry.CONTENT_URI, values);

            if (newUri == null) {
                saveReviewRecordNumber = SAVE_REVIEW_FAIL;
                Log.e(TAG, getString(R.string.insert_review_failed) + i);
            } else {
                saveReviewRecordNumber = SAVE_REVIEW_SUCCESS;
                Log.i(TAG, getString(R.string.insert_review_successful) + i);
            }
        }
    }

    /**
     * Save trailer into database.
     */
    private void saveFavoriteTrailer() {

        for (int i = 0; i < Integer.valueOf(mNumberOfTrailerString); i++) {
            ContentValues values = new ContentValues();
            values.put(TrailerEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
            values.put(TrailerEntry.COLUMN_KEY_OF_TRAILER, mCurrentMovieTrailers.get(i).getKeyString());

            Uri newUri = getContentResolver().insert(TrailerEntry.CONTENT_URI, values);

            if (newUri == null) {
                saveTrailerRecordNumber = SAVE_TRAILER_FAIL;
                Log.e(TAG, getString(R.string.insert_trailer_failed) + i);
            } else {
                saveTrailerRecordNumber = SAVE_TRAILER_SUCCESS;
                Log.i(TAG, getString(R.string.insert_trailer_successful) + i);
            }
        }
    }

    /**
     * Delete movie from database.
     */
    private void deleteFavoriteMovie() {
        String selection = MovieEntry.COLUMN_MOVIE_ID;
        String[] selectionArgs = {mCurrentMovie.getId()};
        int rowsDeleted = getContentResolver().delete(MovieEntry.CONTENT_URI, selection, selectionArgs);

        if (rowsDeleted == 0) {
            deleteMovieRecordNumber = DELETE_MOVIE_FAIL;
            Log.e(TAG, getString(R.string.delete_movie_movie_failed));
        } else {
            deleteMovieRecordNumber = DELETE_MOVIE_SUCCESS;
            Log.i(TAG, getString(R.string.delete_movie_movie_successful));
        }
    }

    /**
     * Delete review from database.
     */
    private void deleteFavoriteReview() {
        String selection = ReviewEntry.COLUMN_MOVIE_ID;
        String[] selectionArgs = {mCurrentMovie.getId()};
        int rowsDeleted = getContentResolver().delete(ReviewEntry.CONTENT_URI, selection, selectionArgs);

        if (rowsDeleted == 0) {
            deleteReviewRecordNumber = DELETE_REVIEW_FAIL;
            Log.e(TAG, getString(R.string.delete_review_failed));
        } else {
            deleteReviewRecordNumber = DELETE_REVIEW_SUCCESS;
            Log.i(TAG, rowsDeleted + getString(R.string.delete_review_successful));
        }
    }

    /**
     * Delete trailer from database
     */
    private void deleteFavoriteTrailer() {
        String selection = TrailerEntry.COLUMN_MOVIE_ID;
        String[] selectionArgs = {mCurrentMovie.getId()};
        int rowsDeleted = getContentResolver().delete(TrailerEntry.CONTENT_URI, selection, selectionArgs);

        if (rowsDeleted == 0) {
            deleteTrailerRecordNumber = DELETE_TRAILER_FAIL;
            Log.e(TAG, getString(R.string.delete_trailer_failed));
        } else {
            deleteTrailerRecordNumber = DELETE_TRAILER_SUCCESS;
            Log.i(TAG, rowsDeleted + getString(R.string.delete_trailer_successful));
        }
    }

    public void loadReviewDataFromDatabase(String movieId) {
        // Create an empty ArrayList that can start adding reviews to
        List<Review> reviews = new ArrayList<>();

        String selection = ReviewEntry.COLUMN_MOVIE_ID;
        String[] selectionArgs = {movieId};
        // Perform a query on the provider using the ContentResolver.
        // Use the {@link ReviewEntry#CONTENT_URI} to access the review data.
        Cursor cursor = getContentResolver().query(
                ReviewEntry.CONTENT_URI,    // The content URI of the movie table
                null,                       // The columns to return for each row
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR));
                String review_content = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_REVIEW_CONTENT));

                // Create a new {@link Review} object with the author and review_content
                // from the cursor response.
                Review review = new Review(author, review_content);

                // Add the new {@link Review} to the list of movies.
                reviews.add(review);
                cursor.moveToNext();
            }
            cursor.close();
        }

        if (reviews != null) {
            mCurrentMovieReviews = reviews;
            mReviewAdapter.setReviewData(reviews);
            // Display total number of reviews in the detail activity, because some movies does
            // not have reviews.
            mNumberOfReviewString = Integer.toString(mReviewAdapter.getItemCount());
            mDetailBinding.tvNumberOfUserReview.setText(mNumberOfReviewString);
        }
    }

    public boolean checkIsMovieAlreadyInFavDatabase(String movieId) {
        SQLiteDatabase database = mMovieDbHelper.getReadableDatabase();
        String selectString = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE "
                + MovieEntry.COLUMN_MOVIE_ID + " =?";
        Cursor cursor = database.rawQuery(selectString, new String[]{movieId});
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count > 0;
    }

    public int setFabButtonStarColor() {
        int colorOfStar = R.color.colorWhiteFavoriteStar;
        try {
            boolean movieIsInDatabase = checkIsMovieAlreadyInFavDatabase(mCurrentMovie.getId());
            if (movieIsInDatabase) {
                colorOfStar = R.color.colorYellowFavoriteStar;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return colorOfStar;
    }
}
