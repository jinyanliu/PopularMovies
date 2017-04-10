package se.sugarest.jane.popularmovies.trailer;

/**
 * Created by jane on 17-4-10.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import se.sugarest.jane.popularmovies.R;

/**
 * {@link TrailerAdapter} exposes a list of current movie reviews to a
 * {@link RecyclerView}
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private static final String TAG = TrailerAdapter.class.getSimpleName();

    private List<Trailer> mTrailerData;

    private String[] mKeyStrings;

    public TrailerAdapter() {
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
        trailerAdapterViewHolder.mKeyTextView.setText(mKeyStrings[position]);
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
    public void setReviewData(List<Trailer> trailerData) {
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
    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mKeyTextView;

        public TrailerAdapterViewHolder(View view) {
            super(view);

            mKeyTextView = (TextView)view.findViewById(R.id.key);


        }
    }
}
