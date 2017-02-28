package se.sugarest.jane.popularmovies.utilities;

/**
 * Created by jane on 2/27/17.
 */

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the movie server:
 * https://developers.themoviedb.org/3/getting-started
 * The Movie Database API
 */
public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIE_BASE_URL =
            "https://api.themoviedb.org/3/discover/movie";

    private static final String API_KEY =
            "YOUR_API_KEY";

    // The format I want the API to return
    private static final String FORMAT = "json";

    private static final String API_KEY_PARAM = "api_key";
    private static final String FORMAT_PARAM = "format";

    /**
     * Builds the URL used to talk to the movie server using a sorByMethod(popularity or top_rated).
     * This sortByMethod is based on the query capabilities of the movie server.
     * <p>
     * Note: The right URL to query movie data looks like:
     * https://api.themoviedb.org/3/discover/movie?popularity&api_key=[YOUR_API_KEY]&format=json
     *
     * @param sortByMethod The sortByMethod that will be queried for.
     * @return the URL to use to query the movie server.
     */
    public static URL buildUrl(String sortByMethod) {
        Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .query(sortByMethod)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(FORMAT_PARAM, FORMAT)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
