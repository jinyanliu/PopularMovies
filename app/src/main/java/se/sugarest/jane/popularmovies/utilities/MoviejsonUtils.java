package se.sugarest.jane.popularmovies.utilities;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.sugarest.jane.popularmovies.R;

/**
 * Created by jane on 2/27/17.
 */

/**
 * Utility functions to handle MovieDB JSON data.
 */
public class MoviejsonUtils {

    private static final String TAG = MoviejsonUtils.class.getSimpleName();

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * containing movie posters URLs Strings.
     *
     * @param movieJSON JSON response from server.
     * @return Array of Strings containing movie posters URLs Strings.
     * @throws JSONException If JSON data cannot be properly parsed.
     */
    public static String[] getSimpleMoviePostersStringsFromJson(Context context, String movieJSON)
            throws JSONException {

        // String Array to hold each movie's posters URL String.
        String[] parsedMoviePostersData = null;

        JSONObject baseJsonResponse = new JSONObject(movieJSON);

        JSONArray movieArray = baseJsonResponse.getJSONArray("results");

        parsedMoviePostersData = new String[movieArray.length()];

        final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_RESOLUTION = "w185/";

        for (int i = 0; i < movieArray.length(); i++) {
            // Get the JSON Object representing the single movie.
            JSONObject currentMovie = movieArray.getJSONObject(i);
            // Get the "poster_path" key value String and store it in poster_path variable.
            String poster_path = currentMovie.getString("poster_path");

            if (poster_path != null && !poster_path.isEmpty() && !poster_path.equals("null")) {
                parsedMoviePostersData[i] = BASE_IMAGE_URL.concat(IMAGE_RESOLUTION).concat(poster_path);
            } else {
                Log.w(TAG, String.valueOf(R.string.poster_path_null) + i);
            }
        }
        return parsedMoviePostersData;
    }
}
