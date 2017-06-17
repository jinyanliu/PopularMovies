package se.sugarest.jane.popularmovies.movie;

/**
 * Created by jane on 17-6-17.
 */

public class MovieBasicInfo {

    private String mId;

    private String mExternalUrl;

    public MovieBasicInfo(String mId, String mExternalUrl) {
        this.mId = mId;
        this.mExternalUrl = mExternalUrl;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmExternalUrl() {
        return mExternalUrl;
    }

    public void setmExternalUrl(String mExternalUrl) {
        this.mExternalUrl = mExternalUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieBasicInfo that = (MovieBasicInfo) o;

        if (mId != null ? !mId.equals(that.mId) : that.mId != null) return false;
        return mExternalUrl != null ? mExternalUrl.equals(that.mExternalUrl) : that.mExternalUrl == null;

    }

    @Override
    public int hashCode() {
        int result = mId != null ? mId.hashCode() : 0;
        result = 31 * result + (mExternalUrl != null ? mExternalUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MovieBasicInfo{" +
                "mId='" + mId + '\'' +
                ", mExternalUrl='" + mExternalUrl + '\'' +
                '}';
    }
}
