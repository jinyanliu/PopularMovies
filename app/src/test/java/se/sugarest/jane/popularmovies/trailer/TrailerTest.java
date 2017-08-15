package se.sugarest.jane.popularmovies.trailer;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by jane on 17-8-15.
 */
public class TrailerTest {

    @Test
    public void getKeyStringTest() throws Exception {
        String keyOfTrailer = "c38r-SAnTWM";
        Trailer trailer = new Trailer(keyOfTrailer);
        Assert.assertEquals(keyOfTrailer, trailer.getKeyString());
    }
}