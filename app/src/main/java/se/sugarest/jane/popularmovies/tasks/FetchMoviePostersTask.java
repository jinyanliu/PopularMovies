package se.sugarest.jane.popularmovies.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-6-15.
 */

public class FetchMoviePostersTask extends AsyncTask<String, Void, List<Movie>> {

    private static final String TAG = PersistMovieTask.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";

    MainActivity mainActivity;

    public FetchMoviePostersTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (this.mainActivity.getmSwipeRefreshLayout().isRefreshing()) {
            this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
        } else {
            this.mainActivity.getmLoadingIndicator().setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected List<Movie> doInBackground(String... params) {

        // If there's no sortBy method, there's no way of showing movies.
        if (params.length == 0) {
            return Collections.emptyList();
        }

        String sortByMethod = params[0];
        URL movieRequestUrl = NetworkUtils.buildUrl(sortByMethod);

        try {
            String jsonMovieResponse = NetworkUtils
                    .getResponseFromHttpUrl(movieRequestUrl);
            List<Movie> simpleJsonMovieData = MovieJsonUtils
                    .extractResultsFromJson(jsonMovieResponse);
            return simpleJsonMovieData;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    protected void onPostExecute(List<Movie> movieData) {
        this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
        this.mainActivity.showMovieDataView();
        this.mainActivity.getmSwipeRefreshLayout().setRefreshing(false);

        if (movieData != null) {
            int count = movieData.size();
            ArrayList<String> array = new ArrayList<>(count);

            String orderBy = getPreference();

            if ("popular".equals(orderBy)) {
                for (int i = 0; i < count; i++) {
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());
                    array.add(i, fullMoviePosterForOneMovie);
                }
            } else {
                for (int i = 0; i < count; i++) {
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());
                    array.add(i, fullMoviePosterForOneMovie);
                }
            }

            Log.i(TAG, "Pass movie data to main activity: " + array.size());
            this.mainActivity.getmMovieAdapter().setMoviePosterData(array);
            // this.mainActivity.getmMovieAdapter().notifyDataSetChanged();

        }
    }

    @NonNull
    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
        return sharedPrefs.getString(
                this.mainActivity.getString(R.string.settings_order_by_key),
                this.mainActivity.getString(R.string.settings_order_by_default)
        );
    }
}
