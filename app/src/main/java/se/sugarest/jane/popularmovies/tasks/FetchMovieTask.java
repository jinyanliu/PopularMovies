package se.sugarest.jane.popularmovies.tasks;

import android.os.AsyncTask;
import android.view.View;

import java.net.URL;
import java.util.List;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-4-21.
 */
public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

    MainActivity mainActivity;

    public FetchMovieTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mainActivity.getmLoadingIndicator().setVisibility(View.VISIBLE);
    }

    @Override
    protected List<Movie> doInBackground(String... params) {

        // If there's no sortBy method, there's no way of showing movies.
        if (params.length == 0) {
            return null;
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
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Movie> movieData) {
        this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
        if (movieData != null) {
            this.mainActivity.getmMovieAdapter().setMoviePosterData(movieData);
        } else {
            this.mainActivity.showErrorMessage();
        }
    }
}
