package se.sugarest.jane.popularmovies.review;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import se.sugarest.jane.popularmovies.R;

/**
 * Created by jane on 17-4-8.
 */

/**
 * {@link ReviewAdapter} exposes a list of current movie reviews to a
 * {@link RecyclerView}
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private static final String TAG = ReviewAdapter.class.getSimpleName();

    private List<Review> mReviewData;

    private String[] mAuthorStrings;

    private String[] mContentStrings;

    public ReviewAdapter() {
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
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_user_review;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, update the contents of the ViewHolder to display the review
     * authors and contents for each particular position, using the "position" argument that
     * is conveniently passed in.
     *
     * @param reviewAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                contents of the item at the given position in the data set.
     * @param position                The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder reviewAdapterViewHolder, int position) {
        reviewAdapterViewHolder.mReviewAuthor.setText(mAuthorStrings[position]);
        reviewAdapterViewHolder.mReviewContent.setText(mContentStrings[position]);
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available on the review section.
     */
    @Override
    public int getItemCount() {
        if (mReviewData == null) {
            return 0;
        }
        return mReviewData.size();
    }

    /**
     * This method is used to set the review authors and contents on a ReviewAdapter if we've already
     * created one. This is handy when getting new data from the web but don't want to create a
     * new ReviewAdapter to display it.
     *
     * @param reviewData The new review data to be displayed.
     */
    public void setReviewData(List<Review> reviewData) {
        mReviewData = reviewData;
        String[] arrayAuthor = new String[reviewData.size()];
        String[] arrayContent = new String[reviewData.size()];

        for (int i = 0; i < reviewData.size(); i++) {
            String currentReviewAuthor = reviewData.get(i).getAuthor();
            String currentReviewContent = reviewData.get(i).getReviewContent();
            arrayAuthor[i] = currentReviewAuthor;
            arrayContent[i] = currentReviewContent;
        }

        mAuthorStrings = arrayAuthor;
        mContentStrings = arrayContent;
        notifyDataSetChanged();

    }

    /**
     * Cache of the children views for a review.
     */
    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mReviewAuthor;
        public final TextView mReviewContent;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mReviewAuthor = (TextView) view.findViewById(R.id.list_item_user_review_author_name);
            mReviewContent = (TextView) view.findViewById(R.id.list_item_user_review_content);
        }
    }
}
