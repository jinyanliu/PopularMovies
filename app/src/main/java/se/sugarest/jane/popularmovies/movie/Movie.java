package se.sugarest.jane.popularmovies.movie;

import java.io.Serializable;

/**
 * Created by jane on 3/1/17.
 */

/**
 * Represents a Movie.
 * It contains the poster url string, the original title, the poster image thumbnail url string,
 * a plot synopsis, user rating, release date and id of a movie.
 */
public class Movie implements Serializable {

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W185 = "w185/";
    private final String IMAGE_SIZE_W780 = "w780/";

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
     * User id of the movie
     */
    private String mId;

    /**
     * Construct a new {@link Movie} object.
     *
     * @param posterPath                is the poster url string of the movie
     * @param originalTitle             is the original title of the movie
     * @param moviePosterImageThumbnail is the poster image thumbnail url string of the movie
     * @param aPlotSynopsis             is a plot synopsis of the movie
     * @param userRating                is the user rating of the movie
     * @param releaseDate               is the release date of the movie
     * @param id                        is the id of the movie
     */
    public Movie(String posterPath, String originalTitle, String moviePosterImageThumbnail, String aPlotSynopsis,
                 String userRating, String releaseDate, String id) {
        mPosterPath = posterPath;
        mOriginalTitle = originalTitle;
        mMoviePosterImageThumbnail = moviePosterImageThumbnail;
        mAPlotSynopsis = aPlotSynopsis;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
        mId = id;
    }

    /**
     * Get the poster path of the movie
     */
    public String getPosterPath() {
        return BASE_IMAGE_URL.concat(IMAGE_SIZE_W185).concat(mPosterPath);
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
        return BASE_IMAGE_URL.concat(IMAGE_SIZE_W780).concat(mMoviePosterImageThumbnail);
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

    /**
     * Get id of the movie
     */
    public String getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "BASE_IMAGE_URL='" + BASE_IMAGE_URL + '\'' +
                ", IMAGE_SIZE_W185='" + IMAGE_SIZE_W185 + '\'' +
                ", IMAGE_SIZE_W780='" + IMAGE_SIZE_W780 + '\'' +
                ", mPosterPath='" + mPosterPath + '\'' +
                ", mOriginalTitle='" + mOriginalTitle + '\'' +
                ", mMoviePosterImageThumbnail='" + mMoviePosterImageThumbnail + '\'' +
                ", mAPlotSynopsis='" + mAPlotSynopsis + '\'' +
                ", mUserRating='" + mUserRating + '\'' +
                ", mReleaseDate='" + mReleaseDate + '\'' +
                ", mId='" + mId + '\'' +
                '}';
    }
}

