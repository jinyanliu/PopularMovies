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
 * {@link TrailerAdapter} exposes a list of current movie trailers to a
 * {@link RecyclerView}
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private static final String TAG = TrailerAdapter.class.getSimpleName();

    private List<Trailer> mTrailerData;

    private String[] mKeyStrings;

    private Context mContext;

    /**
     * And On-click handler that defined tp make it easy for an Activity to interface with
     * the RecyclerView
     */
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

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder trailerAdapterViewHolder, int position) {
        int currentColor = getTrailerFabBackgroundColor(position);
        trailerAdapterViewHolder.mPlayTrailerFab.setBackgroundTintList(ColorStateList
                .valueOf(currentColor));
    }

    /**
     * This method simply returns the number of items to play.
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

    /**
     * This method is used to pass a trailer key string on a TrailerAdapter if we've already
     * created one. This is handy when getting new data from the web but don't want to create a
     * new TrailerAdapter to display it.
     *
     * @param trailerData The new trailer data to be played.
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

    /**
     * Cache of the children views for a trailer.
     */
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

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailerAdapterOnClickHandler {
        void onClick(String trailerSourceKey);
    }

    /**
     * Use switch statement to set the different background color on trailer play icon.
     */
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
