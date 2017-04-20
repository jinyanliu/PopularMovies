package se.sugarest.jane.popularmovies.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.R;

/**
 * Created by jane on 2/27/17.
 */

/**
 * Utility functions to handle MovieDB JSON data.
 */
public class MovieJsonUtils {

    private static final String TAG = MovieJsonUtils.class.getSimpleName();

    /**
     * @return a list of {@link Movie} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<Movie> extractResultsFromJson(String movieJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        // Create an empty ArrayList that can start adding movies to
        List<Movie> movies = new ArrayList<>();

        /**
         * Try to parse the JSON response string. If there's a problem with the way the JSON
         * is formatted, a JSONException object will be thrown.
         * Catch the exception so the app doesn't crash, and print the error message to the logs.
         */
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieJSON);

            // Extract the JSONArray associated with the key called "results",
            // Which represents a list of results (or movies).
            JSONArray movieArray = baseJsonResponse.getJSONArray("results");

            // For each movie in the movieArray, create a {@link Movie} object
            for (int i = 0; i < movieArray.length(); i++) {

                // Get a single movie at position i within the list of movies
                JSONObject currentMovie = movieArray.getJSONObject(i);

                // Get the "poster_path" key value String and store it in poster_path variable.
                String poster_path = currentMovie.getString("poster_path");

                // Get the "original_title" key value String and store it in original_title variable.
                String original_title = currentMovie.getString("original_title");

                // Get the "backdrop_path" key value String and store it in movie_poster_image_thumbnail variable.
                String movie_poster_image_thumbnail = currentMovie.getString("backdrop_path");

                // Get the "overview" key value String and store it in a_plot_synopsis variable.
                String a_plot_synopsis = currentMovie.getString("overview");

                // Get the "vote_average" key value String and store it in user_rating variable.
                String user_rating = currentMovie.getString("vote_average");

                // Get the "release_date" key value String and store it in release_date variable.
                String release_date = currentMovie.getString("release_date");

                // Get the "id" key value String and store it in id variable.
                String id = currentMovie.getString("id");

                // Create a new {@link Movie} object with the poster_path, original_title,
                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
                // from the JSON response.
                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
                        , a_plot_synopsis, user_rating, release_date, id);

                // Add the new {@link Movie} to the list of movies.
                movies.add(movie);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, String.valueOf(R.string.log_error_message_passing_JSON), e);
        }

        // Return the list of movies
        return movies;
    }
}
