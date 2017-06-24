package se.sugarest.jane.popularmovies.movie;

/**
 * Created by jane on 17-6-17.
 */

import java.io.Serializable;

/**
 * Represents a Movie and with external storage information.
 * It contains the poster url string, the original title, the poster image thumbnail url string,
 * a plot synopsis, user rating, release date, id and the external storage information of a movie.
 */
public class FullMovie implements Serializable {

    private String mPosterPath;

    private String mOriginalTitle;

    private String mMoviePosterImageThumbnail;

    private String mAPlotSynopsis;

    private String mUserRating;

    private String mReleaseDate;

    private String mId;

    private String mExternalUrlPosterPath;

    private String mExternalUrlImageThumbnail;


    public FullMovie(String mPosterPath, String mOriginalTitle, String mMoviePosterImageThumbnail, String mAPlotSynopsis, String mUserRating, String mReleaseDate, String mId, String mExternalUrlPosterPath, String mExternalUrlImageThumbnail) {
        this.mPosterPath = mPosterPath;
        this.mOriginalTitle = mOriginalTitle;
        this.mMoviePosterImageThumbnail = mMoviePosterImageThumbnail;
        this.mAPlotSynopsis = mAPlotSynopsis;
        this.mUserRating = mUserRating;
        this.mReleaseDate = mReleaseDate;
        this.mId = mId;
        this.mExternalUrlPosterPath = mExternalUrlPosterPath;
        this.mExternalUrlImageThumbnail = mExternalUrlImageThumbnail;
    }

    public String getmPosterPath() {
        return mPosterPath;
    }

    public String getmOriginalTitle() {
        return mOriginalTitle;
    }

    public String getmMoviePosterImageThumbnail() {
        return mMoviePosterImageThumbnail;
    }

    public String getmAPlotSynopsis() {
        return mAPlotSynopsis;
    }

    public String getmUserRating() {
        return mUserRating;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmId() {
        return mId;
    }

    public String getmExternalUrlPosterPath() {
        return mExternalUrlPosterPath;
    }

    public String getmExternalUrlImageThumbnail() {
        return mExternalUrlImageThumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FullMovie fullMovie = (FullMovie) o;

        if (mPosterPath != null ? !mPosterPath.equals(fullMovie.mPosterPath) : fullMovie.mPosterPath != null)
            return false;
        if (mOriginalTitle != null ? !mOriginalTitle.equals(fullMovie.mOriginalTitle) : fullMovie.mOriginalTitle != null)
            return false;
        if (mMoviePosterImageThumbnail != null ? !mMoviePosterImageThumbnail.equals(fullMovie.mMoviePosterImageThumbnail) : fullMovie.mMoviePosterImageThumbnail != null)
            return false;
        if (mAPlotSynopsis != null ? !mAPlotSynopsis.equals(fullMovie.mAPlotSynopsis) : fullMovie.mAPlotSynopsis != null)
            return false;
        if (mUserRating != null ? !mUserRating.equals(fullMovie.mUserRating) : fullMovie.mUserRating != null)
            return false;
        if (mReleaseDate != null ? !mReleaseDate.equals(fullMovie.mReleaseDate) : fullMovie.mReleaseDate != null)
            return false;
        if (mId != null ? !mId.equals(fullMovie.mId) : fullMovie.mId != null) return false;
        if (mExternalUrlPosterPath != null ? !mExternalUrlPosterPath.equals(fullMovie.mExternalUrlPosterPath) : fullMovie.mExternalUrlPosterPath != null)
            return false;
        return mExternalUrlImageThumbnail != null ? mExternalUrlImageThumbnail.equals(fullMovie.mExternalUrlImageThumbnail) : fullMovie.mExternalUrlImageThumbnail == null;

    }

    @Override
    public int hashCode() {
        int result = mPosterPath != null ? mPosterPath.hashCode() : 0;
        result = 31 * result + (mOriginalTitle != null ? mOriginalTitle.hashCode() : 0);
        result = 31 * result + (mMoviePosterImageThumbnail != null ? mMoviePosterImageThumbnail.hashCode() : 0);
        result = 31 * result + (mAPlotSynopsis != null ? mAPlotSynopsis.hashCode() : 0);
        result = 31 * result + (mUserRating != null ? mUserRating.hashCode() : 0);
        result = 31 * result + (mReleaseDate != null ? mReleaseDate.hashCode() : 0);
        result = 31 * result + (mId != null ? mId.hashCode() : 0);
        result = 31 * result + (mExternalUrlPosterPath != null ? mExternalUrlPosterPath.hashCode() : 0);
        result = 31 * result + (mExternalUrlImageThumbnail != null ? mExternalUrlImageThumbnail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FullMovie{" +
                "mPosterPath='" + mPosterPath + '\'' +
                ", mOriginalTitle='" + mOriginalTitle + '\'' +
                ", mMoviePosterImageThumbnail='" + mMoviePosterImageThumbnail + '\'' +
                ", mAPlotSynopsis='" + mAPlotSynopsis + '\'' +
                ", mUserRating='" + mUserRating + '\'' +
                ", mReleaseDate='" + mReleaseDate + '\'' +
                ", mId='" + mId + '\'' +
                ", mExternalUrlPosterPath='" + mExternalUrlPosterPath + '\'' +
                ", mExternalUrlImageThumbnail='" + mExternalUrlImageThumbnail + '\'' +
                '}';
    }
}
