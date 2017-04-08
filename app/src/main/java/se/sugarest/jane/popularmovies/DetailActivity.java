package se.sugarest.jane.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import se.sugarest.jane.popularmovies.utilities.ReviewJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 3/1/17.
 */

public class DetailActivity extends AppCompatActivity {

    private Movie mCurrentMovie;

    private RecyclerView mReviewRecyclerView;

    private ReviewAdapter mReviewAdapter;

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

        // Setup FAB to add favorite movies into database and change FAB color to yellow
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int yellowColorValue = Color.parseColor("#FFEB3B");
                fab.setColorFilter(yellowColorValue);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);

        mReviewRecyclerView = mDetailBinding.recyclerviewMovieReviews;

        mReviewRecyclerView.setLayoutManager(layoutManager);

        /**
         * The ReviewAdapter is responsible for linking the reviews data with the Views that
         * will end up displaying the reviews data.
         */
        mReviewAdapter = new ReviewAdapter();

        /**
         * Setting the adapter attaches it to the RecyclerView in the layout.
         */
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        /**
         * Once all of the views are setup, review data can be load.
         */
        loadReviewData(mCurrentMovie.getId());

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
}
