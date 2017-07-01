package se.sugarest.jane.popularmovies.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import se.sugarest.jane.popularmovies.DetailActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.review.Review;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;
import se.sugarest.jane.popularmovies.utilities.ReviewJsonUtils;

/**
 * Created by jane on 17-4-20.
 */
public class FetchReviewTask extends AsyncTask<String, Void, List<Review>> {

    private static final String TAG = FetchReviewTask.class.getSimpleName();

    DetailActivity detailActivity;
    String movieId;

    public FetchReviewTask(DetailActivity detailActivity) {
        this.detailActivity = detailActivity;
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.detailActivity.setReviewsLoadingIndicator();

    }

    @Override
    protected List<Review> doInBackground(String... params) {
        // If there's no movie id, there's no movie reviews to show.
        if (params.length == 0) {
            return null;
        }
        String id = params[0];
        movieId = id;
        URL movieReviewRequestUrl = NetworkUtils.buildReviewUrl(id);

        try {
            String jsonMovieReviewResponse = NetworkUtils
                    .getResponseFromHttpUrl(movieReviewRequestUrl);
            List<Review> simpleJsonReviewData = ReviewJsonUtils
                    .extractResultsFromMovieReviewJson(jsonMovieReviewResponse);
            return simpleJsonReviewData;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Review> reviewData) {
        this.detailActivity.hideLoadingIndicators();
        if (reviewData != null) {
            this.detailActivity.setmCurrentMovieReviews(reviewData);
            this.detailActivity.getmReviewAdapter().setReviewData(reviewData);
            // Display total number of reviews in the detail activity, because some movies does
            // not have reviews.
            String numberOfReviewString = Integer.toString(this.detailActivity.getmReviewAdapter().getItemCount());
            this.detailActivity.setmNumberOfReviewString(numberOfReviewString);
            this.detailActivity.setNumberOfReviewTextViewText(numberOfReviewString);
            boolean movieIsInDatabase = this.detailActivity.checkIsMovieAlreadyInFavDatabase(movieId);
            if (movieIsInDatabase) {
                this.detailActivity.saveFavoriteReview();
                Log.i(TAG, "Save Reviews.");
            }
        } else {
            Log.e(TAG, detailActivity.getString(R.string.log_error_message_offline_before_fetch_review_finish));

            this.detailActivity.getmDetailBinding().extraDetails.ivReviewLoadingIndicator.setVisibility(View.INVISIBLE);

            String expectedMsg = detailActivity.getString(R.string.toast_message_offline_before_fetch_review_finish);

            if (this.detailActivity.getmToast() != null) {
                String displayedText = ((TextView) ((LinearLayout) this.detailActivity.getmToast().getView())
                        .getChildAt(0)).getText().toString();
                if (!displayedText.equals(expectedMsg)) {
                    this.detailActivity.getmToast().cancel();
                    Toast newToast = Toast.makeText(detailActivity, detailActivity.getString(R.string.toast_message_offline_before_fetch_review_finish), Toast.LENGTH_SHORT);
                    this.detailActivity.setmToast(newToast);
                    this.detailActivity.getmToast().setGravity(Gravity.BOTTOM, 0, 0);
                    this.detailActivity.getmToast().show();
                }
            } else {
                Toast newToast = Toast.makeText(detailActivity, expectedMsg, Toast.LENGTH_SHORT);
                this.detailActivity.setmToast(newToast);
                this.detailActivity.getmToast().setGravity(Gravity.BOTTOM, 0, 0);
                this.detailActivity.getmToast().show();
            }

        }
    }
}
