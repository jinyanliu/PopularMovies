package se.sugarest.jane.popularmovies.utilities.JsonUtils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.sugarest.jane.popularmovies.review.Review;
import timber.log.Timber;

/**
 * Created by jane on 17-4-8.
 */

/**
 * Utility file for parsing Review JSON.
 */
public class ReviewJsonUtils {

    /**
     * @param movieReviewJSON the given JSON response.
     * @return a list of {@link Review} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<Review> extractResultsFromMovieReviewJson(String movieReviewJSON) {

        // If the JSON string is empty or null, then return empty instead of null.
        if (TextUtils.isEmpty(movieReviewJSON)) {
            return Collections.emptyList();
        }

        // Create an empty ArrayList that can start adding reviews to
        List<Review> reviews = new ArrayList<>();

        /*
        Try to parse the JSON response string. If there's a problem with the way the JSON is formatted,
        a JSONException object will be thrown.
        Catch the exception so the app doesn't crash, and print the error message to the logs.
        */
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieReviewJSON);

            // Extract the JSONArray associated with the key called "results",
            // Which represents a list of results (or reviews).
            JSONArray reviewArray = baseJsonResponse.getJSONArray("results");

            // For each review in the reviewArray, create a Review object
            for (int i = 0; i < reviewArray.length(); i++) {

                // Get a single review at position i within the list of reviews
                JSONObject currentReview = reviewArray.getJSONObject(i);

                // Get the "author" key value String and store it in author variable.
                String author = currentReview.getString("author");

                // Get the "content" key value String and store it in content variable.
                String content = currentReview.getString("content");

                // Create a new Review object with the author and content from the JSON response.
                Review review = new Review(author, content);

                // Add the new Review to the list of reviews.
                reviews.add(review);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.e(e, "Failed to pass Review JSON.");
        }

        // Return the list of reviews
        return reviews;
    }
}
