package se.sugarest.jane.popularmovies.utilities.JsonUtils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.sugarest.jane.popularmovies.trailer.Trailer;
import timber.log.Timber;

/**
 * Created by jane on 17-4-10.
 */

/**
 * Utility file for parsing Trailer JSON.
 */
public class TrailerJsonUtils {

    /**
     * @param movieTrailerJSON the given JSON response.
     * @return a list of {@link Trailer} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<Trailer> extractResultsFromMovieTrailerJson(String movieTrailerJSON) {

        // If the JSON string is empty or null, then return empty instead of null.
        if (TextUtils.isEmpty(movieTrailerJSON)) {
            return Collections.emptyList();
        }

        // Create an empty ArrayList that can start adding trailers to
        List<Trailer> trailers = new ArrayList<>();

        /*
        Try to parse the JSON response string. If there's a problem with the way the JSON is formatted,
        a JSONException object will be thrown.
        Catch the exception so the app doesn't crash, and print the error message to the logs.
        */
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieTrailerJSON);

            // Extract the JSONArray associated with the key called "youtube",
            // Which represents a list of trailers.
            JSONArray trailerArray = baseJsonResponse.getJSONArray("youtube");

            // For each trailer in the trailerArray, create a Trailer object
            for (int i = 0; i < trailerArray.length(); i++) {

                // Get a single trailer at position i within the list of trailers
                JSONObject currentTrailer = trailerArray.getJSONObject(i);

                // Get the "source" key value String and store it in key variable.
                String key = currentTrailer.getString("source");

                // Create a new Trailer object with the key from the JSON response.
                Trailer trailer = new Trailer(key);

                // Add the new Trailer to the list of trailers.
                trailers.add(trailer);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.e(e, "Failed to pass Trailer JSON.");
        }
        // Return the list of trailers
        return trailers;
    }
}


