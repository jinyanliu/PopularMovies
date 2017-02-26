package se.sugarest.jane.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by jane on 2/26/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    /**
     * An On-click handler that we've defined to make it easy for an Activity to interface with
     * the RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;
    private int[] mMoviePostersResources;

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;

    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    /**
     *
     * @param movieAdapterViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        int moviePosterForOneMovie = mMoviePostersResources[position];
        movieAdapterViewHolder.mMoviePosterImageView.setImageResource(moviePosterForOneMovie);
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available on the screen
     */
    @Override
    public int getItemCount() {
        if (mMoviePostersResources == null) return 0;
        return mMoviePostersResources.length;
    }

    /**
     * This method is used to set the movie posters on a MovieAdapter if we've already
     * created one. This is handy when getting new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param moviePosterData The new movie poster data to be displayed.
     */
    public void setMoviePosterData(int[] moviePosterData) {
        mMoviePostersResources = moviePosterData;
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(int moviePosterIdThatWasClicked);
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

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int moviePosterIdThatWasClicked = mMoviePostersResources[adapterPosition];
            mClickHandler.onClick(moviePosterIdThatWasClicked);
        }
    }

}





