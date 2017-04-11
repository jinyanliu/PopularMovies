package se.sugarest.jane.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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
import android.view.View;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

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

    private Movie mCurrentMovie;

    private RecyclerView mReviewRecyclerView;

    private RecyclerView mTrailerRecyclerView;

    private ReviewAdapter mReviewAdapter;

    private TrailerAdapter mTrailerAdapter;

    /**
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DateBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Setup fab_favorite to add favorite movies into database and change FAB color to yellow
        final FloatingActionButton fab_favorite = (FloatingActionButton) findViewById(R.id.fab_favorite);
        fab_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_favorite.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorYellowFavoriteStar));
            }
        });

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mCurrentMovie = (Movie) getIntent().getExtras().getSerializable("movie");
            }
        }

        // Set current movie original title on the detail activity menu bar as activity's title.
        setTitle(mCurrentMovie.getOriginalTitle());

        // Set current movie poster image thumbnail
        Picasso.with(DetailActivity.this).load(mCurrentMovie.getMoviePosterImageThumbnail())
                .into(mDetailBinding.ivMoviePosterImageThumbnail);

        // Set current movie textViews content
        mDetailBinding.tvUserRating.setText(mCurrentMovie.getUserRating());
        mDetailBinding.tvReleaseDate.setText(mCurrentMovie.getReleaseDate());
        mDetailBinding.tvAPlotSynopsis.setText(mCurrentMovie.getAPlotSynopsis());

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

    }

    /**
     * This method will use the pass in movie id to tell the background method to get the
     * movie review data in the background.
     *
     * @param id The id of the movie clicked.
     */
    private void loadReviewData(String id) {
        new FetchReviewTask().execute(id);
    }

    /**
     * This method will use the pass in movie id to tell the background method to get the
     * movie trailer data in the background.
     *
     * @param id The id of the movie clicked.
     */
    private void loadTrailerData(String id) {
        new FetchTrailerTask().execute(id);
    }

    /**
     * This method is overridden by the DetailActivity class in order to handle RecyclerView item
     * clicks.
     *
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
                mReviewAdapter.setReviewData(reviewData);
                // Display total number of reviews in the detail activity, because some movies does
                // not have reviews.
                String numberOfReviewString = Integer.toString(mReviewAdapter.getItemCount());
                mDetailBinding.tvNumberOfUserReview.setText(numberOfReviewString);
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
                mTrailerAdapter.setReviewData(trailerData);
            }
        }
    }
}
