package se.sugarest.jane.popularmovies;

/**
 * Created by jane on 3/1/17.
 */

public class Movie {

    /**
     * Poster Url String of the movie
     */
    private String mPosterPath;

    /**
     * Original Title of the movie
     */
    private String mOriginalTitle;

    /**
     * Poster Image Thumbnail Url String of the movie
     */
    private String mMoviePosterImageThumbnail;

    /**
     * A Plot Synopsis of the movie
     */
    private String mAPlotSynopsis;

    /**
     * User rating of the movie
     */
    private String mUserRating;

    /**
     * Release Date of the movie
     */
    private String mReleaseDate;

    /**
     * Construct a new {@link Movie} object.
     *
     * @param posterPath                is the poster url string of the movie
     * @param originalTitle             is the original title of the movie
     * @param moviePosterImageThumbnail is the poster image thumbnail url string of the movie
     * @param aPlotSynopsis             is a plot synopsis of the movie
     * @param userRating                is the user rating of the movie
     * @param releaseDate               is the release date of the movie
     */
    public Movie(String posterPath, String originalTitle, String moviePosterImageThumbnail, String aPlotSynopsis, String userRating, String releaseDate) {
        mPosterPath = posterPath;
        mOriginalTitle = originalTitle;
        mMoviePosterImageThumbnail = moviePosterImageThumbnail;
        mAPlotSynopsis = aPlotSynopsis;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
    }

    /**
     * Get the poster path of the movie
     */
    public String getPosterPath() {
        return mPosterPath;
    }

    /**
     * Get original title of the movie
     */
    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    /**
     * Get movie poster image thumbnail url string of the movie
     */
    public String getMoviePosterImageThumbnail() {
        return mMoviePosterImageThumbnail;
    }

    /**
     * Get a plot synopsis of the movie
     */
    public String getAPlotSynopsis() {
        return mAPlotSynopsis;
    }

    /**
     * Get user rating of the movie
     */
    public String getUserRating() {
        return mUserRating;
    }

    /**
     * Get release date of the movie
     */
    public String getReleaseDate() {
        return mReleaseDate;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "mPosterPath='" + mPosterPath + '\'' +
                ", mOriginalTitle='" + mOriginalTitle + '\'' +
                ", mMoviePosterImageThumbnail='" + mMoviePosterImageThumbnail + '\'' +
                ", mAPlotSynopsis='" + mAPlotSynopsis + '\'' +
                ", mUserRating='" + mUserRating + '\'' +
                ", mReleaseDate='" + mReleaseDate + '\'' +
                '}';
    }
}

