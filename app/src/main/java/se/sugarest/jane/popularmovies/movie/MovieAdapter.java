package se.sugarest.jane.popularmovies.movie;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularPosterEntry;

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

    private Cursor mCursor;

    /**
     * An On-click handler that we've defined to make it easy for an Activity to interface with
     * the RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;

    private String[] mMoviePostersUrlStrings;

    private Context mContext;

    private List<Movie> mMoveData;

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     * @param context      The context to pass down for {@link Picasso}.with(mContext)...
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler, Context context) {
        mClickHandler = clickHandler;
        mContext = context;
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
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            mCursor.moveToPosition(position);
            int moviePosterColumnIndex = mCursor
                    .getColumnIndex(MovieContract.CacheMovieMostPopularEntry.COLUMN_POSTER_PATH);
            String moviePosterForOneMovie = mCursor.getString(moviePosterColumnIndex);
            String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                    .concat(moviePosterForOneMovie);

            // String moviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
            // .concat(mMoviePostersUrlStrings[position]);
            // Log.i(TAG, "Loading ".concat(moviePosterForOneMovie));
            // If there is a network connection, fetch poster data from web
            Picasso.with(mContext)
                    .load(fullMoviePosterForOneMovie)
                    .error(R.drawable.picasso_placeholder_error)
                    //.placeholder(R.drawable.picasso_placeholder_loading)
                    .into(movieAdapterViewHolder.mMoviePosterImageView);

            // new FetchExternalStorageMoviePosterImagesTask(mContext).execute(fullMoviePosterForOneMovie);

        } else {
            // setMoviePosterData();

            String[] projection = {CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH};

            String selection = CacheMovieMostPopularPosterEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(position)};
            Cursor cursor = mContext.getContentResolver().query(
                    CacheMovieMostPopularPosterEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);


//            if (cursor != null && cursor.getCount() > 0) {
//                String[] mMoviePostersUrlStrings = new String[cursor.getCount()];
//                int i = 0;
//
//                cursor.moveToFirst();
//
//                while (!cursor.isAfterLast()) {
//                    mMoviePostersUrlStrings[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH));
//                    i++;
//                    cursor.moveToNext();
//                }
//            }

//            String moviePosterForOneMovie = mMoviePostersUrlStrings[position];

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String moviePosterForOneMovie = cursor
                        .getString(cursor.getColumnIndex(CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH));
                Picasso.with(mContext)
                        .load(moviePosterForOneMovie)
                        .error(R.drawable.picasso_placeholder_error)
                        //.placeholder(R.drawable.picasso_placeholder_loading)
                        .into(movieAdapterViewHolder.mMoviePosterImageView);
            }
            cursor.close();

        }
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available on the screen
     */
    @Override
    public int getItemCount() {
//        if (mMoviePostersUrlStrings == null) return 0;
//        return mMoviePostersUrlStrings.length;

        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * This method is used to set the movie posters on a MovieAdapter from external storage posters urls.
     */
//    public void setMoviePosterData() {
//
//        // storeMoviePostersUrlStringsIntoExternalDatabase();
//
//        String[] projection = {CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH};
//        String selection = CacheMovieMostPopularPosterEntry._ID +"=?";
//        String[] selectionArgs = {};
//        Cursor cursor = mContext.getContentResolver().query(
//                CacheMovieMostPopularPosterEntry.CONTENT_URI,
//                projection,
//                null,
//                null,
//                null);
//
//
//        if (cursor != null && cursor.getCount() > 0) {
//            String[] array = new String[cursor.getCount()];
//            int i = 0;
//
//            cursor.moveToFirst();
//
//            while (!cursor.isAfterLast()) {
//                array[i] = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularPosterEntry.COLUMN_POSTER_PATH));
//                i++;
//                cursor.moveToNext();
//            }
//
//            mMoviePostersUrlStrings = array;
//            notifyDataSetChanged();
//        }
//    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        // void onClick(Movie currentMovie);
        void onClick(String movieTitle);
    }

    /**
     * Cache of the children views for a movie poster image.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView mMoviePosterImageView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.iv_movie_posters);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int movieTitleColumnIndex = mCursor
                    .getColumnIndex(MovieContract.CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE);


            // Movie currentMovieData = mMoveData.get(adapterPosition);
            mClickHandler.onClick(mCursor.getString(movieTitleColumnIndex));
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

//    public void storeMoviePostersUrlStringsIntoExternalDatabase() {
//        String[] projection = {CacheMovieMostPopularEntry.COLUMN_POSTER_PATH};
//        Cursor cursor = mContext.getContentResolver().query(
//                CacheMovieMostPopularEntry.CONTENT_URI,
//                projection,
//                null,
//                null,
//                null);
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
//                String fullPosterPathToBeDownload = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
//                        .concat(poster_path);
//                new FetchExternalStorageMoviePosterImagesTask(mContext).execute(fullPosterPathToBeDownload);
//                cursor.moveToNext();
//            }
//        }
//        cursor.close();
//    }
}





