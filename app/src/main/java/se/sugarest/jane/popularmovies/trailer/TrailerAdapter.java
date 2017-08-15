package se.sugarest.jane.popularmovies.trailer;

/**
 * Created by jane on 17-4-10.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.List;

import se.sugarest.jane.popularmovies.R;

/**
 * Exposes a list of current movie trailers to a {@link RecyclerView}.
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private List<Trailer> mTrailerData;

    private String[] mKeyStrings;

    private Context mContext;

    // Add On-click handler that defined to make it easy for an Activity to interface with the RecyclerView
    private final TrailerAdapterOnClickHandler mClickHandler;

    /**
     * Creates a TrailerAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public TrailerAdapter(TrailerAdapterOnClickHandler clickHandler, Context context) {
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
     * @return A new ReviewAdapterViewHolder that holds the View for each list item
     */
    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, update the contents of the ViewHolder to display the different
     * background color for each particular position, using the "position" argument that
     * is conveniently passed in.
     *
     * @param trailerAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                 contents of the item at the given position in the data set.
     * @param position                 The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder trailerAdapterViewHolder, int position) {
        int currentColor = getTrailerFabBackgroundColor(position);
        trailerAdapterViewHolder.mPlayTrailerFab.setBackgroundTintList(ColorStateList
                .valueOf(currentColor));
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available on the trailer section.
     */
    @Override
    public int getItemCount() {
        if (mTrailerData == null) {
            return 0;
        }
        return mTrailerData.size();
    }

    /*
    This method is used to pass a trailer key string on a TrailerAdapter if we've already created one.
    This is handy when getting new data from the web but don't want to create a new TrailerAdapter to
    display it.
    */
    public void setTrailerData(List<Trailer> trailerData) {
        mTrailerData = trailerData;
        String[] arrayKeyString = new String[trailerData.size()];
        for (int i = 0; i < trailerData.size(); i++) {
            String currentKeyString = trailerData.get(i).getKeyString();
            arrayKeyString[i] = currentKeyString;
        }
        mKeyStrings = arrayKeyString;
        notifyDataSetChanged();

    }

    // Cache of the children views for a trailer.
    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final FloatingActionButton mPlayTrailerFab;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            mPlayTrailerFab = (FloatingActionButton) view.findViewById(R.id.fab_trailer_icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String currentTrailerSourceKey = mKeyStrings[adapterPosition];
            mClickHandler.onClick(currentTrailerSourceKey);
        }
    }

    // The interface that receives onClick messages.
    public interface TrailerAdapterOnClickHandler {
        void onClick(String trailerSourceKey);
    }

    // Use switch statement to set the different background color on trailer play icon.
    private int getTrailerFabBackgroundColor(int position) {
        int trailerFabBackgroundColorId;
        switch (position) {
            case 0:
                trailerFabBackgroundColorId = R.color.trailer0;
                break;
            case 1:
                trailerFabBackgroundColorId = R.color.trailer1;
                break;
            case 2:
                trailerFabBackgroundColorId = R.color.trailer2;
                break;
            case 3:
                trailerFabBackgroundColorId = R.color.trailer3;
                break;
            case 4:
                trailerFabBackgroundColorId = R.color.trailer4;
                break;
            case 5:
                trailerFabBackgroundColorId = R.color.trailer5;
                break;
            case 6:
                trailerFabBackgroundColorId = R.color.trailer6;
                break;
            case 7:
                trailerFabBackgroundColorId = R.color.trailer7;
                break;
            case 8:
                trailerFabBackgroundColorId = R.color.trailer8;
                break;
            case 9:
                trailerFabBackgroundColorId = R.color.trailer9;
                break;
            default:
                trailerFabBackgroundColorId = R.color.trailer10;
                break;
        }
        return ContextCompat.getColor(mContext, trailerFabBackgroundColorId);
    }
}
