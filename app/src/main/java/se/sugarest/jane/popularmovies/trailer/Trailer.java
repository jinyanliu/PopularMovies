package se.sugarest.jane.popularmovies.trailer;

/**
 * Created by jane on 17-4-10.
 */

/**
 * Represents a Trailer.
 * It contains the trailer source key for Youtube Intent use.
 */
public class Trailer {

    // Key string of a trailer
    private String mKeyOfTrailer;

    /**
     * Constructs a new {@link Trailer} object.
     *
     * @param key is the key string of a trailer.
     */
    public Trailer(String key) {
        mKeyOfTrailer = key;
    }

    // Gets the key string of a trailer
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
