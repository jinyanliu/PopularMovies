package se.sugarest.jane.popularmovies.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.review.Review;

/**
 * Created by jane on 17-4-8.
 */

public class ReviewJsonUtils {
    private static final String TAG = ReviewJsonUtils.class.getSimpleName();

    /**
     * @return a list of {@link Review} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<Review> extractResultsFromMovieReviewJson(String movieReviewJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieReviewJSON)) {
            return null;
        }

        // Create an empty ArrayList that can start adding reviews to
        List<Review> reviews = new ArrayList<>();

        /**
         * Try to parse the JSON response string. If there's a problem with the way the JSON
         * is formatted, a JSONException object will be thrown.
         * Catch the exception so the app doesn't crash, and print the error message to the logs.
         */
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieReviewJSON);

            // Extract the JSONArray associated with the key called "results",
            // Which represents a list of results (or reviews).
            JSONArray reviewArray = baseJsonResponse.getJSONArray("results");

            // For each review in the reviewArray, create a {@link Review} object
            for (int i = 0; i < reviewArray.length(); i++) {

                // Get a single review at position i within the list of reviews
                JSONObject currentReview = reviewArray.getJSONObject(i);

                // Get the "author" key value String and store it in author variable.
                String author = currentReview.getString("author");

                // Get the "content" key value String and store it in content variable.
                String content = currentReview.getString("content");

                // Create a new {@link Review} object with the author and content
                // from the JSON response.
                Review review = new Review(author, content);

                // Add the new {@link Review} to the list of reviews.
                reviews.add(review);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, String.valueOf(R.string.log_error_message_passing_JSON), e);
        }

        // Return the list of movies
        return reviews;
    }
}
