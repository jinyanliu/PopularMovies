package se.sugarest.jane.popularmovies.movie;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;
import se.sugarest.jane.popularmovies.ui.MainActivity;
import se.sugarest.jane.popularmovies.utilities.ExternalPathUtils;

/**
 * Created by jane on 2/26/17.
 */

/**
 * {@link MovieAdapter} exposes a list of movie posters to a
 * {@link android.support.v7.widget.RecyclerView}
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W185 = "w185/";
    private final String CACHE_POSTERS_FOLDER_NAME = "/cacheposters/";

    private Cursor mCursor;
    private boolean mLoadFromDb;

    /**
     * An On-click handler that we've defined to make it easy for an Activity to interface with
     * the RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;

    private ArrayList<String> mMoviePostersUrlStrings = new ArrayList<>();

    private MainActivity mainActivity;

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     * @param mainActivity The context to pass down for {@link Picasso}.with(mainActivity)...
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler, MainActivity mainActivity) {
        mClickHandler = clickHandler;
        this.mainActivity = mainActivity;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If RecyclerView has more than one type of item (which this one don't)
     *                  this viewType can be used to provide a different layout.
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_movie;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, update the contents of the ViewHolder to display the movie
     * posters for each particular position, using the "position" argument that is conveniently
     * passed in.
     *
     * @param movieAdapterViewHolder The ViewHolder which should be updated to represent the
     *                               contents of the item at the given position in the data set.
     * @param position               The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder movieAdapterViewHolder, final int position) {

        // Those animation is a substitute for Picasso's placeholder.
        Animation a = AnimationUtils.loadAnimation(mainActivity, R.anim.progress_animation_main);
        a.setDuration(1000);
        movieAdapterViewHolder.mLoadingImageView.startAnimation(a);

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            setMoviePostersOnline(movieAdapterViewHolder, position);
        } else {
            setMoviePostersOffline(movieAdapterViewHolder, position);
        }

        if (mCursor != null) {
            // set ContentDescription for every poster image.
            String original_title = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
            movieAdapterViewHolder.mMoviePosterImageView.setContentDescription(original_title);
        }
    }

    private void setMoviePostersOffline(final MovieAdapterViewHolder movieAdapterViewHolder, final int position) {

        String basePosterExternalUrl = ExternalPathUtils.getExternalPathBasicFileName(this.mainActivity)
                + CACHE_POSTERS_FOLDER_NAME;

        String orderBy = getPreference();
        if ("popular".equals(orderBy)) {
            mCursor.moveToPosition(position);
            String moviePosterForOneMovie = mCursor.getString(mCursor
                    .getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));

            String fullMoviePosterForOneMovie = basePosterExternalUrl
                    .concat(moviePosterForOneMovie);

            final File pathToPic = new File(fullMoviePosterForOneMovie);

            Picasso.with(mainActivity)
                    // PosterPath from external storage
                    .load(pathToPic)
                    .error(R.drawable.pic_error_loading_w370)
                    .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                            Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE)));
                            mCursor.moveToPosition(position);
                            String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
                            if (currentMovieTitle.contains(":")) {
                                String[] separated = currentMovieTitle.split(":");
                                // separate[1].trim() will remove the empty space to the second string
                                movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                            } else {
                                movieAdapterViewHolder.mErrorMovieNameTextView
                                        .setText(currentMovieTitle);
                            }
                            if (pathToPic.exists()) {
                                pathToPic.delete();
                            }
                        }
                    });

        } else if ("top_rated".equals(orderBy)) {
            mCursor.moveToPosition(position);
            String moviePosterForOneMovie = mCursor.getString(mCursor
                    .getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));

            String fullMoviePosterForOneMovie = basePosterExternalUrl
                    .concat(moviePosterForOneMovie);
            final File pathToPic = new File(fullMoviePosterForOneMovie);

            Picasso.with(mainActivity)
                    // PosterPath from external storage
                    .load(pathToPic)
                    .error(R.drawable.pic_error_loading_w370)
                    .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                            Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE)));
                            mCursor.moveToPosition(position);
                            String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE));
                            if (currentMovieTitle.contains(":")) {
                                String[] separated = currentMovieTitle.split(":");
                                // separate[1].trim() will remove the empty space to the second string
                                movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                            } else {
                                movieAdapterViewHolder.mErrorMovieNameTextView
                                        .setText(currentMovieTitle);
                            }
                            if (pathToPic.exists()) {
                                pathToPic.delete();
                            }
                        }
                    });
        } else {
            mCursor.moveToPosition(position);
            String moviePosterForOneMovie = mCursor.getString(mCursor
                    .getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));

            String fullMovieFavPosterForOneMovie = basePosterExternalUrl
                    .concat(moviePosterForOneMovie);
            final File pathToPic = new File(fullMovieFavPosterForOneMovie);

            Picasso.with(mainActivity)
                    // PosterPath from external storage
                    .load(pathToPic)
                    .error(R.drawable.pic_error_loading_w370)
                    .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                            Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_ORIGINAL_TITLE)));
                            mCursor.moveToPosition(position);
                            String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_ORIGINAL_TITLE));
                            if (currentMovieTitle.contains(":")) {
                                String[] separated = currentMovieTitle.split(":");
                                // separate[1].trim() will remove the empty space to the second string
                                movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                            } else {
                                movieAdapterViewHolder.mErrorMovieNameTextView
                                        .setText(currentMovieTitle);
                            }
                            if (pathToPic.exists()) {
                                pathToPic.delete();
                            }
                        }
                    });
        }
    }

    private void setMoviePostersOnline(final MovieAdapterViewHolder movieAdapterViewHolder, final int position) {
        String orderBy = getPreference();

        if (!"favorites".equals(orderBy)) {
            if (mLoadFromDb == false) {
                String moviePosterForOneMovie = mMoviePostersUrlStrings.get(position);

                Picasso.with(mainActivity)
                        .load(moviePosterForOneMovie)
                        .error(R.drawable.pic_error_loading_w370)
                        .into(movieAdapterViewHolder.mMoviePosterImageView);
            } else {
                if ("popular".equals(orderBy)) {
                    mCursor.moveToPosition(position);
                    String moviePosterForOneMovie = mCursor.getString(mCursor
                            .getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(moviePosterForOneMovie);

                    Picasso.with(mainActivity)
                            // PosterPath from web
                            .load(fullMoviePosterForOneMovie)
                            .error(R.drawable.pic_error_loading_w370)
                            .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onError() {
                                    movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                                    Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE)));
                                    mCursor.moveToPosition(position);
                                    String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
                                    if (currentMovieTitle.contains(":")) {
                                        String[] separated = currentMovieTitle.split(":");
                                        // separate[1].trim() will remove the empty space to the second string
                                        movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                                    } else {
                                        movieAdapterViewHolder.mErrorMovieNameTextView
                                                .setText(currentMovieTitle);
                                    }
                                }
                            });
                } else {
                    mCursor.moveToPosition(position);
                    String moviePosterForOneMovie = mCursor.getString(mCursor
                            .getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(moviePosterForOneMovie);

                    Picasso.with(mainActivity)
                            // PosterPath from web
                            .load(fullMoviePosterForOneMovie)
                            .error(R.drawable.pic_error_loading_w370)
                            .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onError() {
                                    movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                                    Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE)));
                                    mCursor.moveToPosition(position);
                                    String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE));
                                    if (currentMovieTitle.contains(":")) {
                                        String[] separated = currentMovieTitle.split(":");
                                        // separate[1].trim() will remove the empty space to the second string
                                        movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                                    } else {
                                        movieAdapterViewHolder.mErrorMovieNameTextView
                                                .setText(currentMovieTitle);
                                    }
                                }
                            });
                }
            }
        } else {
            mCursor.moveToPosition(position);
            String moviePosterForOneMovie = mCursor.getString(mCursor
                    .getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
            String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                    .concat(moviePosterForOneMovie);

            Picasso.with(mainActivity)
                    // PosterPath from web
                    .load(fullMoviePosterForOneMovie)
                    .error(R.drawable.pic_error_loading_w370)
                    .into(movieAdapterViewHolder.mMoviePosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            movieAdapterViewHolder.mErrorMovieNameTextView.setVisibility(View.VISIBLE);
                            Log.i(TAG, "Current Position: " + position + "\nCurrent Movie Title: " + mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_ORIGINAL_TITLE)));
                            mCursor.moveToPosition(position);
                            String currentMovieTitle = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_ORIGINAL_TITLE));
                            if (currentMovieTitle.contains(":")) {
                                String[] separated = currentMovieTitle.split(":");
                                // separate[1].trim() will remove the empty space to the second string
                                movieAdapterViewHolder.mErrorMovieNameTextView.setText(separated[0] + ":" + "\n" + separated[1].trim());
                            } else {
                                movieAdapterViewHolder.mErrorMovieNameTextView
                                        .setText(currentMovieTitle);
                            }
                        }
                    });
        }
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available on the screen
     */
    @Override
    public int getItemCount() {

        if (mLoadFromDb) {
            if (null == mCursor) {
                return 0;
            }
            return mCursor.getCount();
        } else {
            return mMoviePostersUrlStrings.size();
        }
    }

    /**
     * This method is used to set the movie posters on a MovieAdapter from very first time of installing.
     * Without store any information, just to display the posters fast.
     */
    public void setMoviePosterData(ArrayList<String> moviePostersUrls) {
        mLoadFromDb = false;
        mMoviePostersUrlStrings.clear();
        mMoviePostersUrlStrings.addAll(moviePostersUrls);
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie currentMovie);

    }

    /**
     * Cache of the children views for a movie poster image.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView mMoviePosterImageView;

        public final ImageView mLoadingImageView;

        public final TextView mErrorMovieNameTextView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.iv_movie_posters);
            mLoadingImageView = (ImageView) view.findViewById(R.id.iv_loading);
            mErrorMovieNameTextView = (TextView) view.findViewById(R.id.tv_error_movie_title_display);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCursor != null) {
                String orderBy = getPreference();

                if ("popular".equals(orderBy)) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);

                    String poster_path = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
                    String original_title = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
                    String movie_poster_image_thumbnail =
                            mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                    String a_plot_synopsis = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
                    String user_rating = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
                    String release_date = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
                    String id = mCursor.getString(mCursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));

                    // Create a new {@link Movie} object with the poster_path, original_title,
                    // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
                    // from the cursor response.
                    Movie currentMovieData = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                            , a_plot_synopsis, user_rating, release_date, id);
                    mClickHandler.onClick(currentMovieData);
                } else if ("top_rated".equals(orderBy)) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);

                    String poster_path = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH));
                    String original_title = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE));
                    String movie_poster_image_thumbnail =
                            mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                    String a_plot_synopsis = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS));
                    String user_rating = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_USER_RATING));
                    String release_date = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE));
                    String id = mCursor.getString(mCursor.getColumnIndex(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID));

                    // Create a new {@link Movie} object with the poster_path, original_title,
                    // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
                    // from the cursor response.
                    Movie currentMovieData = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                            , a_plot_synopsis, user_rating, release_date, id);
                    mClickHandler.onClick(currentMovieData);
                } else {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);

                    String poster_path = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                    String original_title = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_ORIGINAL_TITLE));
                    String movie_poster_image_thumbnail =
                            mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                    String a_plot_synopsis = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_A_PLOT_SYNOPSIS));
                    String user_rating = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_USER_RATING));
                    String release_date = mCursor.getString(mCursor.getColumnIndex(FavMovieEntry.COLUMN_RELEASE_DATE));
                    String id = mCursor.getString(mCursor.getColumnIndex(MovieContract.FavMovieEntry.COLUMN_MOVIE_ID));

                    // Create a new {@link Movie} object with the poster_path, original_title,
                    // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
                    // from the cursor response.
                    Movie currentMovieData = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                            , a_plot_synopsis, user_rating, release_date, id);
                    mClickHandler.onClick(currentMovieData);
                }
            } else {
                if (mainActivity.getmToast() != null) {
                    mainActivity.getmToast().cancel();
                }
                Toast newToast = Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_message_movies_not_load_yet), Toast.LENGTH_SHORT);
                mainActivity.setmToast(newToast);
                mainActivity.getmToast().setGravity(Gravity.BOTTOM, 0, 0);
                mainActivity.getmToast().show();
            }
        }
    }

    public void swapCursor(Cursor newCursor) {
        mLoadFromDb = true;
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @NonNull
    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        return sharedPrefs.getString(
                mainActivity.getString(R.string.settings_order_by_key),
                mainActivity.getString(R.string.settings_order_by_default)
        );
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }
}





