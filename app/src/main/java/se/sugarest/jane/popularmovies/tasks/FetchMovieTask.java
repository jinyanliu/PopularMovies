package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;

import java.net.URL;
import java.util.List;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;

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
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
            String orderBy = sharedPrefs.getString(
                    this.mainActivity.getString(R.string.settings_order_by_key),
                    this.mainActivity.getString(R.string.settings_order_by_default)
            );

            if ("popular".equals(orderBy)) {
                int count = movieData.size();
                ContentValues[] cvArray = new ContentValues[count];
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());
                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, movieData.get(i).getId());
                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());
                    values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());
                    values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());
                    values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());
                    cvArray[i] = values;
                }
                this.mainActivity.getContentResolver().bulkInsert(
                        CacheMovieMostPopularEntry.CONTENT_URI,
                        cvArray);
            } else {
                int count = movieData.size();
                ContentValues[] cvArray = new ContentValues[count];
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());
                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, movieData.get(i).getId());
                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());
                    values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());
                    values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());
                    values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());
                    cvArray[i] = values;
                }
                this.mainActivity.getContentResolver().bulkInsert(
                        CacheMovieTopRatedEntry.CONTENT_URI,
                        cvArray);
            }

        }
    }
}
