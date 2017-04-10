package se.sugarest.jane.popularmovies.trailer;

/**
 * Created by jane on 17-4-10.
 */

/**
 * Represents a Trailer.
 * It contains the trailer source key for Youtube Intent use.
 */
public class Trailer {

    /**
     * Key string of a trailer
     */
    private String mKeyOfTrailer;

    /**
     * Constructs a new {@link Trailer} object.
     *
     * @param key is the key string of a trailer.
     */
    public Trailer(String key) {
        mKeyOfTrailer = key;
    }

    /**
     * Get the key string of the trailer
     */
    public String getKeyString() {
        return mKeyOfTrailer;
    }

    @Override
    public String toString() {
        return "Trailer{" +
                "mKeyOfTrailer='" + mKeyOfTrailer + '\'' +
                '}';
    }
}
