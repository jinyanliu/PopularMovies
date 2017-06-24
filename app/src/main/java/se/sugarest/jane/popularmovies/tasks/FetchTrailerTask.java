package se.sugarest.jane.popularmovies.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.util.List;

import se.sugarest.jane.popularmovies.DetailActivity;
import se.sugarest.jane.popularmovies.trailer.Trailer;
import se.sugarest.jane.popularmovies.utilities.NetworkUtils;
import se.sugarest.jane.popularmovies.utilities.TrailerJsonUtils;

/**
 * Created by jane on 17-4-20.
 */
public class FetchTrailerTask extends AsyncTask<String, Void, List<Trailer>> {

    DetailActivity detailActivity;
    String movieId;

    private static final String TAG = FetchTrailerTask.class.getSimpleName();

    public FetchTrailerTask(DetailActivity detailActivity) {
        this.detailActivity = detailActivity;
    }

    @Override
    protected List<Trailer> doInBackground(String... params) {
        // If there's no movie id, there's no movie trailers to show.
        if (params.length == 0) {
            return null;
        }
        String id = params[0];
        movieId = id;
        URL movieTrailerRequestUrl = NetworkUtils.buildTrailerUrl(id);

        try {
            String jsonMovieTrailerResponse = NetworkUtils
                    .getResponseFromHttpUrl(movieTrailerRequestUrl);
            List<Trailer> simpleJsonTrailerData = TrailerJsonUtils
                    .extractResultsFromMovieTrailerJson(jsonMovieTrailerResponse);
            return simpleJsonTrailerData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Trailer> trailerData) {
        this.detailActivity.hideLoadingIndicators();
        // Because we need to use trailerData.get(0), so when trailerData.size() = 0, we cannot pass it.
        if (trailerData != null) {
            this.detailActivity.setmCurrentMovieTrailers(trailerData);
            this.detailActivity.getmTrailerAdapter().setTrailerData(trailerData);
            String numberOfTrailerString = Integer.toString(this.detailActivity.getmTrailerAdapter().getItemCount());
            this.detailActivity.setmNumberOfTrailerString(numberOfTrailerString);
            this.detailActivity.getmDetailBinding().extraDetails.tvNumberOfTrailer.setText(numberOfTrailerString);
            // When movie is saved offline (no fetching reviews and trailers), and reopen online, reviews and trailers show.
            // Save secretly reviews and trailers for user.
            boolean movieIsInDatabase = this.detailActivity.checkIsMovieAlreadyInFavDatabase(movieId);
            if (movieIsInDatabase) {
                this.detailActivity.saveFavoriteTrailer();
                Log.i(TAG, "Save Trailers.");
            }
        }
        if (trailerData.size() > 0) {
            this.detailActivity.setmFirstTrailerSourceKey(trailerData.get(0).getKeyString());
        } else {
            this.detailActivity.setmFirstTrailerSourceKey(null);
        }
    }
}
