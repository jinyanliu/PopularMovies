package se.sugarest.jane.popularmovies.tasks;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import se.sugarest.jane.popularmovies.MainActivity;
import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.data.MovieContract;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.movie.Movie;
import se.sugarest.jane.popularmovies.utilities.MovieJsonUtils;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;

/**
 * Created by jane on 17-4-21.
 */
public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

    private static final String TAG = FetchMovieTask.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";

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

        if (movieData != null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mainActivity);
            String orderBy = sharedPrefs.getString(
                    this.mainActivity.getString(R.string.settings_order_by_key),
                    this.mainActivity.getString(R.string.settings_order_by_default)
            );

            if ("popular".equals(orderBy)) {

                // When latest movie data fetches, delete CacheMovieMostPopularTable
                this.mainActivity.getContentResolver().delete(
                        CacheMovieMostPopularEntry.CONTENT_URI,
                        null,
                        null);

                // When latest movie data fetches, delete CacheMovieMostPopularPosterTable
                this.mainActivity.getContentResolver().delete(
                        MovieContract.CacheMovieMostPopularPosterEntry.CONTENT_URI,
                        null,
                        null);

                // When latest movie data fetches, delete External Storage Folder
                File popularMoviePicsFolder
                        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + "/popularmovies/");
                popularMoviePicsFolder.delete();

                int count = movieData.size();
                Vector<ContentValues> cVVector = new Vector<ContentValues>(count);
                // ContentValues[] cvArray = new ContentValues[count];
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());
                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID, movieData.get(i).getId());
                    values.put(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());
                    values.put(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());

                    values.put(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());

                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(movieData.get(i).getPosterPath());

                    new FetchExternalStorageMoviePosterImagesTask(this.mainActivity).execute(fullMoviePosterForOneMovie);

                    values.put(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieMostPopularEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                    cVVector.add(values);

                    //cvArray[i] = values;
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int bulkInsertRows = this.mainActivity.getContentResolver().bulkInsert(
                            CacheMovieMostPopularEntry.CONTENT_URI,
                            cvArray);
                    if (bulkInsertRows == cVVector.size()) {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular successful.");
                    } else {
                        Log.i(TAG, "bulkInsertCacheMovie MostPopular unsuccessful.");
                    }
                }


                // this.mainActivity.restartLoader();


//                for (int i = 0; i < count; i++) {
//                    String urlToBeDownLoaded = movieData.get(i).getMoviePosterImageThumbnail();
//                    new FetchExternalStorageMoviePosterImagesTask(this.mainActivity).execute(urlToBeDownLoaded);
//                }

                // showDataBaseCacheMovieMostPopularPoster();


            } else {

                this.mainActivity.getContentResolver().delete(
                        CacheMovieTopRatedEntry.CONTENT_URI,
                        null,
                        null);

                int count = movieData.size();
                Vector<ContentValues> cVVector = new Vector<ContentValues>(count);
                // ContentValues[] cvArray = new ContentValues[count];
                for (int i = 0; i < count; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CacheMovieTopRatedEntry.COLUMN_A_PLOT_SYNOPSIS, movieData.get(i).getAPlotSynopsis());
                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_ID, movieData.get(i).getId());
                    values.put(CacheMovieTopRatedEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL, movieData.get(i).getMoviePosterImageThumbnail());
                    values.put(CacheMovieTopRatedEntry.COLUMN_ORIGINAL_TITLE, movieData.get(i).getOriginalTitle());
                    values.put(CacheMovieTopRatedEntry.COLUMN_POSTER_PATH, movieData.get(i).getPosterPath());
                    values.put(CacheMovieTopRatedEntry.COLUMN_RELEASE_DATE, movieData.get(i).getReleaseDate());
                    values.put(CacheMovieTopRatedEntry.COLUMN_USER_RATING, movieData.get(i).getUserRating());

                    cVVector.add(values);
                    // cvArray[i] = values;
                }

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int bulkInsertRows = this.mainActivity.getContentResolver().bulkInsert(
                            CacheMovieTopRatedEntry.CONTENT_URI,
                            cvArray);
                    if (bulkInsertRows == cVVector.size()) {
                        Log.i(TAG, "bulkInsertCacheMovie TopRated successful.");
                    } else {
                        Log.i(TAG, "bulkInsertCacheMovie TopRated unsuccessful.");
                    }
                }


//                this.mainActivity.getContentResolver().bulkInsert(
//                        CacheMovieTopRatedEntry.CONTENT_URI,
//                        cvArray);

//                for (int i = 0; i < count; i++) {
//                    String urlToBeDownLoaded = movieData.get(i).getMoviePosterImageThumbnail();
//                    new FetchExternalStorageMoviePosterImagesTask(this.mainActivity).execute(urlToBeDownLoaded);
//                }
            }

        }


        this.mainActivity.initCursorLoader();


    }



//    private void showDataBaseCacheMovieMostPopularPoster() {
//
//        this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
//
//        // Create an empty ArrayList that can start adding movies to
//        List<Movie> movies = new ArrayList<>();
//
//        // Perform a query on the provider using the ContentResolver.
//        // Use the {@link CacheMovieMostPopularEntry#CONTENT_URI} to access the movie data.
//        Cursor cursor = this.mainActivity.getContentResolver().query(
//                CacheMovieMostPopularEntry.CONTENT_URI, // The content URI of the cache movie most popular table
//                null,                                   // The columns to return for each row
//                null,
//                null,
//                null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                String poster_path = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_POSTER_PATH));
//                String original_title = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_ORIGINAL_TITLE));
//                String movie_poster_image_thumbnail =
//                        cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
//                String a_plot_synopsis = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_A_PLOT_SYNOPSIS));
//                String user_rating = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_USER_RATING));
//                String release_date = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_RELEASE_DATE));
//                String id = cursor.getString(cursor.getColumnIndex(CacheMovieMostPopularEntry.COLUMN_MOVIE_ID));
//
//                // Create a new {@link Movie} object with the poster_path, original_title,
//                // movie_poster_image_thumbnail, a_plot_synopsis, user_rating, release_date,id
//                // from the cursor response.
//                Movie movie = new Movie(poster_path, original_title, movie_poster_image_thumbnail
//                        , a_plot_synopsis, user_rating, release_date, id);
//
//                // Add the new {@link Movie} to the list of movies.
//                movies.add(movie);
//                cursor.moveToNext();
//            }
//            cursor.close();
//            this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            this.mainActivity.getmMovieAdapter().setMoviePosterData(movies);
//        } else {
//            this.mainActivity.getmLoadingIndicator().setVisibility(View.INVISIBLE);
//            this.mainActivity.showErrorMessage();
//            this.mainActivity.getmErrorMessageDisplay().setText(this.mainActivity.getString(R.string.error_message_no_popular_movie));
//        }
//    }

}
