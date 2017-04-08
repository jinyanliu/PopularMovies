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
 * <p>
 * Note: You have to request your own api key.
 */
public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIE_BASE_URL =
            "http://api.themoviedb.org/3/";

    private static final String API_KEY =
            "YOUR_API_KEY";

    private static final String API_KEY_PARAM = "api_key";

    /**
     * Builds the URL used to talk to the movie server using a sorByMethod(popularity or top_rated).
     * This sortByMethod is based on the query capabilities of the movie server.
     * <p>
     * Note: The right URL to query movie data looks like:
     * http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
     *
     * @param sortByMethod The sortByMethod that will be queried for.
     * @return the URL to use to query the movie server.
     */
    public static URL buildUrl(String sortByMethod) {
        String baseUrl = MOVIE_BASE_URL + sortByMethod;
        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
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
     * Builds the URL used to talk to the movie server to fetch movie review data using a movie id.
     * <p>
     * Note: The right URL to query movie review data looks like:
     * http://api.themoviedb.org/3/movie/263115/reviews?api_key=[YOUR_API_KEY]
     *
     * @param id The movie id that will be queried for.
     * @return the URL to use to query the movie server to fetch movie review data.
     */
    public static URL buildReviewUrl(String id) {
        String addToUrl = "movie/" + id + "/reviews";
        String baseUrl = MOVIE_BASE_URL + addToUrl;
        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built Reviews URI " + url);
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
