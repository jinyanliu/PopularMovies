package se.sugarest.jane.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.sugarest.jane.popularmovies.data.MovieContract;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;
import se.sugarest.jane.popularmovies.data.MovieDbHelper;
import se.sugarest.jane.popularmovies.databinding.ActivityDetailBinding;
import se.sugarest.jane.popularmovies.movie.FullMovie;
import se.sugarest.jane.popularmovies.review.Review;
import se.sugarest.jane.popularmovies.review.ReviewAdapter;
import se.sugarest.jane.popularmovies.tasks.FetchReviewTask;
import se.sugarest.jane.popularmovies.tasks.FetchTrailerTask;
import se.sugarest.jane.popularmovies.trailer.Trailer;
import se.sugarest.jane.popularmovies.trailer.TrailerAdapter;
import se.sugarest.jane.popularmovies.trailer.TrailerAdapter.TrailerAdapterOnClickHandler;

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
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String BASE_YOUTUBE_URL_APP = "vnd.youtube:";
    private final String BASE_YOUTUBE_URL_WEB = "http://www.youtube.com/watch?v=";

    private FullMovie mCurrentMovie;

    private List<Review> mCurrentMovieReviews;

    private List<Trailer> mCurrentMovieTrailers;

    /*****************************************************************************************************
     * mCurrentMovieReviews == null , mCurrentMovieTrailers == null : Haven't loaded yet.                *
     * (slow internet speed).                                                                            *
     * mCurrentMovieReviews.size() == 0 , mCurrentMovieTrailers.size() == 0 : No reviews or no trailers. *
     * (Loaded finished successfully.)                                                                   *
     *****************************************************************************************************/

    private RecyclerView mReviewRecyclerView;

    private RecyclerView mTrailerRecyclerView;

    private ReviewAdapter mReviewAdapter;

    private TrailerAdapter mTrailerAdapter;

    private String mNumberOfReviewString;

    private String mNumberOfTrailerString;

    private String mFirstTrailerSourceKey;

    private FloatingActionButton mFabButton;

    private ProgressBar mLoadingIndicator;

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

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ActivityDetailBinding getmDetailBinding() {
        return mDetailBinding;
    }

    public void setmCurrentMovieReviews(List<Review> mCurrentMovieReviews) {
        this.mCurrentMovieReviews = mCurrentMovieReviews;
    }

    public void setmCurrentMovieTrailers(List<Trailer> mCurrentMovieTrailers) {
        this.mCurrentMovieTrailers = mCurrentMovieTrailers;
    }

    public ReviewAdapter getmReviewAdapter() {
        return mReviewAdapter;
    }

    public TrailerAdapter getmTrailerAdapter() {
        return mTrailerAdapter;
    }

    public void setmNumberOfReviewString(String mNumberOfReviewString) {
        this.mNumberOfReviewString = mNumberOfReviewString;
    }

    public void setmNumberOfTrailerString(String mNumberOfTrailerString) {
        this.mNumberOfTrailerString = mNumberOfTrailerString;
    }

    public void setmFirstTrailerSourceKey(String mFirstTrailerSourceKey) {
        this.mFirstTrailerSourceKey = mFirstTrailerSourceKey;
    }

    public Toast getmToast() {
        return mToast;
    }

    public void setmToast(Toast mToast) {
        this.mToast = mToast;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        /**
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mCurrentMovie = (FullMovie) getIntent().getExtras().getSerializable("movie");
            }
        }

        // Set current movie original title on the detail activity menu bar as activity's title.
        setTitle(mCurrentMovie.getmOriginalTitle());

        // Those animation is a substitute for Picasso's placeholder.
        Animation a = AnimationUtils.loadAnimation(this, R.anim.progress_animation_main);
        a.setDuration(1000);
        mDetailBinding.primaryInfo.ivLoading.startAnimation(a);

        /**
         * While online, picasso load url string which starts with "http://";
         * While offline, picasso load file path on external storage which starts with "/storage"
         */
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            setCurrentMovieImageThumbnailOnLine();
        } else {
            setCurrentMovieImageThumbnailOffLine();
        }

        // Set current movie textViews content
        // Some movies' original title is too long to display fully on the action bar, so we need to
        // repeat it again in the detail page.
        if (mCurrentMovie.getmOriginalTitle().contains(":")) {
            String[] separated = mCurrentMovie.getmOriginalTitle().split(":");
            // separate[1].trim() will remove the empty space to the second string
            mDetailBinding.primaryInfo.tvMovieTitle.setText(separated[0] + ":" + "\n" + separated[1].trim());
        } else {
            mDetailBinding.primaryInfo.tvMovieTitle.setText(mCurrentMovie.getmOriginalTitle());
        }
        mDetailBinding.primaryInfo.tvUserRating.setText(mCurrentMovie.getmUserRating());
        mDetailBinding.primaryInfo.tvReleaseDate.setText(mCurrentMovie.getmReleaseDate());
        mDetailBinding.primaryInfo.tvAPlotSynopsis.setText(mCurrentMovie.getmAPlotSynopsis());

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

        mReviewRecyclerView = mDetailBinding.extraDetails.recyclerviewMovieReviews;

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
        loadReviewData(mCurrentMovie.getmId());

        LinearLayoutManager layoutManagerTrailers = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);

        mTrailerRecyclerView = mDetailBinding.extraDetails.recyclerviewMovieTrailers;

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
        loadTrailerData(mCurrentMovie.getmId());

        // Setup fab_favorite to add favorite movies into database and change FAB color to yellow
        View primaryLayout = findViewById(R.id.primary_info);
        mFabButton = (FloatingActionButton) primaryLayout.findViewById(R.id.fab_favorite);

        mFabButton.setColorFilter(ContextCompat.getColor(DetailActivity.this, setFabButtonStarColor()));

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setFabButtonStarColor() == R.color.colorWhiteFavoriteStar) {
                    mFabButton.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorYellowFavoriteStar));
                    try {
                        saveMovie();
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    mFabButton.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorWhiteFavoriteStar));
                    deleteMovie();
                }
            }
        });

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
    }

    private void setCurrentMovieImageThumbnailOffLine() {
        String currentMoviePosterImageThumbnail = mCurrentMovie.getmExternalUrlImageThumbnail();
        File pathToPic = new File(currentMoviePosterImageThumbnail);
        Picasso.with(DetailActivity.this)
                .load(pathToPic)
                .error(R.drawable.picasso_placeholder_error)
                .into(mDetailBinding.primaryInfo.ivMoviePosterImageThumbnail);
    }

    private void setCurrentMovieImageThumbnailOnLine() {
        String currentMoviePosterImageThumbnail = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                .concat(mCurrentMovie.getmMoviePosterImageThumbnail());
        Picasso.with(DetailActivity.this)
                .load(currentMoviePosterImageThumbnail)
                .error(R.drawable.picasso_placeholder_error)
                .into(mDetailBinding.primaryInfo.ivMoviePosterImageThumbnail);
    }

    private void refreshMovie() {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            setCurrentMovieImageThumbnailOnLine();
            loadReviewData(mCurrentMovie.getmId());
            loadTrailerData(mCurrentMovie.getmId());
        } else {
            hideLoadingIndicators();
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(DetailActivity.this, getString(R.string.toast_message_refresh_no_internet), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        }
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
                NetworkInfo networkInfo = getNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new FetchReviewTask(this).execute(id);
                } else {
                    hideLoadingIndicators();
                    setNumberOfReviewTextViewText(getString(R.string.detail_activity_offline_reminder_text));
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void hideLoadingIndicators() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This method will use the pass in movie id to either tell the background method to get the
     * movie trailer data in the background or load the trailer data from the database.
     *
     * @param id The id of the movie clicked.
     */
    private void loadTrailerData(String id) {

        try {
            boolean movieIsInDatabase = checkIsMovieAlreadyInFavDatabase(id);
            // When saved offline, trailers haven't loaded yet.
            if (movieIsInDatabase) {
                loadTrailerDataFromDatabase(id);
            } else {
                NetworkInfo networkInfo = getNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new FetchTrailerTask(this).execute(id);
                } else {
                    hideLoadingIndicators();
                    setNumberOfTrailerTextViewText(getString(R.string.detail_activity_offline_reminder_text));
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void setTrailersLoadingIndicator() {
        mDetailBinding.extraDetails.ivTrailerLoadingIndicator.setVisibility(View.VISIBLE);
        // Trailers Loading Animation
        Animation b = AnimationUtils.loadAnimation(this, R.anim.progress_animation_main);
        b.setDuration(1000);
        mDetailBinding.extraDetails.ivTrailerLoadingIndicator.startAnimation(b);
    }

    public void setNumberOfTrailerTextViewText(String numberOfTrailerTextViewText) {
        mDetailBinding.extraDetails.ivTrailerLoadingIndicator.clearAnimation();
        mDetailBinding.extraDetails.ivTrailerLoadingIndicator.setVisibility(View.GONE);
        mDetailBinding.extraDetails.tvNumberOfTrailer.setText(numberOfTrailerTextViewText);
    }

    public void setReviewsLoadingIndicator() {
        mDetailBinding.extraDetails.ivReviewLoadingIndicator.setVisibility(View.VISIBLE);
        Animation c = AnimationUtils.loadAnimation(this, R.anim.progress_animation_main);
        c.setDuration(1000);
        mDetailBinding.extraDetails.ivReviewLoadingIndicator.startAnimation(c);
    }

    public void setNumberOfReviewTextViewText(String numberOfReviewTextViewText) {
        mDetailBinding.extraDetails.ivReviewLoadingIndicator.clearAnimation();
        mDetailBinding.extraDetails.ivReviewLoadingIndicator.setVisibility(View.GONE);
        mDetailBinding.extraDetails.tvNumberOfUserReview.setText(numberOfReviewTextViewText);
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
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_YOUTUBE_URL_APP + trailerSourceKey));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(BASE_YOUTUBE_URL_WEB + trailerSourceKey));
            try {
                startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                startActivity(webIntent);
            }
        } else {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, getString(R.string.detail_activity_offline_reminder_text), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        }
    }

    /**
     * Save movie, review and trailer into database.
     */
    private void saveMovie() {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (mCurrentMovieReviews != null && mCurrentMovieTrailers != null) {
                if (mCurrentMovieReviews.size() > 0) {
                    if (mCurrentMovieTrailers.size() > 0) {
                        saveFavoriteMovie();
                        saveFavoriteTrailer();
                        saveFavoriteReview();
                        Log.i(TAG, "mCurrentMovieReviews.size() = " + mCurrentMovieReviews.size());
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS && saveReviewRecordNumber == SAVE_REVIEW_SUCCESS
                                && saveTrailerRecordNumber == SAVE_TRAILER_SUCCESS) {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        } else {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    } else {
                        saveFavoriteMovie();
                        saveFavoriteReview();
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS && saveReviewRecordNumber == SAVE_REVIEW_SUCCESS) {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        } else {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    }

                } else {
                    if (mCurrentMovieTrailers.size() > 0) {
                        saveFavoriteMovie();
                        saveFavoriteTrailer();
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS && saveTrailerRecordNumber == SAVE_TRAILER_SUCCESS) {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        } else {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    } else {
                        saveFavoriteMovie();
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS) {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_successful), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        } else {
                            mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    }
                }
            } else {
                mFabButton.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.colorWhiteFavoriteStar));
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, getString(R.string.toast_message_review_and_trailer_not_loaded_yet), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            }
        } else {
            saveFavoriteMovie();
            if (mToast != null) {
                mToast.cancel();
            }
            if (saveMovieRecordNumber == SAVE_MOVIE_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_successful_reviews_trailers_later), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.insert_movie_failed), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            }
        }
    }

    /**
     * Delete movie, review and trailer from database.
     */
    private void deleteMovie() {
        // Happens when movie was saved when offline. So reviews and trailers are both null.
        if (mCurrentMovieReviews == null && mCurrentMovieTrailers == null) {
            deleteFavoriteMovie();
            if (mToast != null) {
                mToast.cancel();
            }
            if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS) {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            } else {
                mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, 0);
                mToast.show();
            }
        } else {
            // Happens when movie was saved when online. So reviews and trailers couldn't be null.
            if (mCurrentMovieReviews.size() > 0) {
                if (mCurrentMovieTrailers.size() > 0) {
                    deleteFavoriteMovie();
                    deleteFavoriteTrailer();
                    deleteFavoriteReview();
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS && deleteReviewRecordNumber == DELETE_REVIEW_SUCCESS
                            && deleteTrailerRecordNumber == DELETE_TRAILER_SUCCESS) {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    } else {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                } else {
                    deleteFavoriteMovie();
                    deleteFavoriteReview();
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS && deleteReviewRecordNumber == DELETE_REVIEW_SUCCESS) {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    } else {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                }
            } else {
                if (mCurrentMovieTrailers.size() > 0) {
                    deleteFavoriteMovie();
                    deleteFavoriteTrailer();
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS && deleteTrailerRecordNumber == DELETE_TRAILER_SUCCESS) {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    } else {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                } else {
                    deleteFavoriteMovie();
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    if (deleteMovieRecordNumber == DELETE_MOVIE_SUCCESS) {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_successful), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    } else {
                        mToast = Toast.makeText(this, getString(R.string.delete_movie_failed), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                }
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
        values.put(FavMovieEntry.COLUMN_POSTER_PATH, mCurrentMovie.getmPosterPath());
        values.put(FavMovieEntry.COLUMN_ORIGINAL_TITLE, mCurrentMovie.getmOriginalTitle());
        values.put(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, mCurrentMovie.getmMoviePosterImageThumbnail());
        values.put(FavMovieEntry.COLUMN_A_PLOT_SYNOPSIS, mCurrentMovie.getmAPlotSynopsis());
        values.put(FavMovieEntry.COLUMN_USER_RATING, mCurrentMovie.getmUserRating());
        values.put(FavMovieEntry.COLUMN_RELEASE_DATE, mCurrentMovie.getmReleaseDate());
        values.put(FavMovieEntry.COLUMN_MOVIE_ID, mCurrentMovie.getmId());
        values.put(FavMovieEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, mCurrentMovie.getmExternalUrlPosterPath());
        values.put(FavMovieEntry.COLUMN_EXTERNAL_STORAGE_IMAGE_THUMBNAIL, mCurrentMovie.getmExternalUrlImageThumbnail());

        // Insert a new movie into the provider, returning the content URI for the new movie.
        Uri newUri = getContentResolver().insert(MovieContract.FavMovieEntry.CONTENT_URI, values);

        // Show a log message depending on whether or not the insertion was successful
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
    public void saveFavoriteReview() {

        for (int i = 0; i < Integer.valueOf(mNumberOfReviewString); i++) {
            ContentValues values = new ContentValues();
            values.put(ReviewEntry.COLUMN_MOVIE_ID, mCurrentMovie.getmId());
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
    public void saveFavoriteTrailer() {

        for (int i = 0; i < Integer.valueOf(mNumberOfTrailerString); i++) {
            ContentValues values = new ContentValues();
            values.put(TrailerEntry.COLUMN_MOVIE_ID, mCurrentMovie.getmId());
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
        String selection = FavMovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = {mCurrentMovie.getmId()};
        int rowsDeleted = getContentResolver().delete(MovieContract.FavMovieEntry.CONTENT_URI, selection, selectionArgs);

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
        String selection = ReviewEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = {mCurrentMovie.getmId()};
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
        String selection = TrailerEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = {mCurrentMovie.getmId()};
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
        setReviewsLoadingIndicator();
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

            if (reviews != null) {
                hideLoadingIndicators();
                mCurrentMovieReviews = reviews;
                mReviewAdapter.setReviewData(reviews);
                // Display total number of reviews in the detail activity, because some movies does
                // not have reviews.
                mNumberOfReviewString = Integer.toString(mReviewAdapter.getItemCount());
                setNumberOfReviewTextViewText(mNumberOfReviewString);
            }
        } else {
            /************************************************************************************************
             * This block of code's function:                                                               *
             * when saved offline without fetching reviews and trailers,                                    *
             * but open this movie when online, refetch reviews and trailers online inside of showing 0. *
             ************************************************************************************************/
            NetworkInfo networkInfo = getNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new FetchReviewTask(this).execute(movieId);
            } else {
                hideLoadingIndicators();
                setNumberOfReviewTextViewText(getString(R.string.detail_activity_offline_reminder_text));
            }
        }
    }

    public void loadTrailerDataFromDatabase(String movieId) {
        setTrailersLoadingIndicator();
        List<Trailer> trailers = new ArrayList<>();
        String selection = TrailerEntry.COLUMN_MOVIE_ID;
        String[] selectionArgs = {movieId};
        Cursor cursor = getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mFirstTrailerSourceKey = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY_OF_TRAILER));
            while (!cursor.isAfterLast()) {
                String trailer_key = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY_OF_TRAILER));
                Trailer trailer = new Trailer(trailer_key);
                trailers.add(trailer);
                cursor.moveToNext();
            }
            cursor.close();
            if (trailers != null) {
                hideLoadingIndicators();
                mCurrentMovieTrailers = trailers;
                mTrailerAdapter.setTrailerData(trailers);
                mNumberOfTrailerString = Integer.toString(mTrailerAdapter.getItemCount());
                setNumberOfTrailerTextViewText(mNumberOfTrailerString);
            }
        } else {
            /************************************************************************************************
             * This block of code's function:                                                               *
             * when saved offline without fetching reviews and trailers,                                    *
             * but open this movie when online, refetch reviews and trailers online inside of showing 0. *
             ************************************************************************************************/
            NetworkInfo networkInfo = getNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new FetchTrailerTask(this).execute(movieId);
            } else {
                hideLoadingIndicators();
                setNumberOfTrailerTextViewText(getString(R.string.detail_activity_offline_reminder_text));
            }
        }
    }

    public boolean checkIsMovieAlreadyInFavDatabase(String movieId) {
        SQLiteDatabase database = mMovieDbHelper.getReadableDatabase();
        String selectString = "SELECT * FROM " + FavMovieEntry.TABLE_NAME + " WHERE "
                + FavMovieEntry.COLUMN_MOVIE_ID + " =?";
        Cursor cursor = database.rawQuery(selectString, new String[]{movieId});
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count > 0;
    }

    public int setFabButtonStarColor() {
        int colorOfStar = R.color.colorWhiteFavoriteStar;
        try {
            boolean movieIsInDatabase = checkIsMovieAlreadyInFavDatabase(mCurrentMovie.getmId());
            if (movieIsInDatabase) {
                colorOfStar = R.color.colorYellowFavoriteStar;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return colorOfStar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
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
            // Share first trailer url
            case R.id.action_share:
                NetworkInfo networkInfo = getNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (mCurrentMovieTrailers != null) {
                        if (mFirstTrailerSourceKey != null) {
                            String urlToShare = BASE_YOUTUBE_URL_WEB + mFirstTrailerSourceKey;
                            shareFirstYoutubeUrl(urlToShare);
                        } else {
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(this, getString(R.string.toast_message_no_trailer_to_share), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    } else {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(this, getString(R.string.toast_message_trailer_not_loaded_yet), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                } else {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(this, getString(R.string.detail_activity_offline_reminder_text), Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.BOTTOM, 0, 0);
                    mToast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareFirstYoutubeUrl(String urlToShare) {
        String mimeType = "text/plain";
        String title = getString(R.string.title_share_url_string);
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(urlToShare)
                .startChooser();
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }
}
