package se.sugarest.jane.popularmovies.utilities;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jane on 2/27/17.
 */

public class OpenMoviejsonUtils {

    private static final String TAG = OpenMoviejsonUtils.class.getSimpleName();

    public static String[] getSimpleMoviePostersStringsFromJson(Context context, String movieJSON)
            throws JSONException {

        String[] parsedMoviePostersData = null;

        JSONObject baseJsonResponse = new JSONObject(movieJSON);

        JSONArray movieArray = baseJsonResponse.getJSONArray("results");

        parsedMoviePostersData = new String[movieArray.length()];

        final String baseImageUrl = "http://image.tmdb.org/t/p/";
        final String imageResolution = "w185/";

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject currentMovie = movieArray.getJSONObject(i);
            String poster_path = currentMovie.getString("poster_path");

            if (poster_path != null && !poster_path.isEmpty() && !poster_path.equals("null")) {
                parsedMoviePostersData[i] = baseImageUrl.concat(imageResolution).concat(poster_path);
            } else {
                Log.w(TAG, "Picture is missing.");
            }

        }

        return parsedMoviePostersData;

    }

}
