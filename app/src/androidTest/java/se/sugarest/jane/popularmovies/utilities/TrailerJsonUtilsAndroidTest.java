package se.sugarest.jane.popularmovies.utilities;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jane on 17-8-15.
 */

public class TrailerJsonUtilsAndroidTest {

    @Test
    public void testExtractResultsFromMovieTrailerJson_emptyJson() {
        String emptyJson = "{}";
        Assert.assertTrue(TrailerJsonUtils.extractResultsFromMovieTrailerJson(emptyJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieTrailerJson_noTrailer() {
        String noTrailerJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"quicktime\": [],\n" +
                "\"youtube\": [\n" +
                "]\n" +
                "}";
        Assert.assertTrue(TrailerJsonUtils.extractResultsFromMovieTrailerJson(noTrailerJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieTrailerJson_invalidJson() {
        String invalidJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"quicktime\": [],\n" +
                "\"youtube\": [\n" +
                //"{\n" +
                "\"name\": \"Official US Teaser Trailer\",\n" +
                "\"size\": \"HD\",\n" +
                "\"source\": \"c38r-SAnTWM\",\n" +
                "\"type\": \"Trailer\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(TrailerJsonUtils.extractResultsFromMovieTrailerJson(invalidJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieTrailerJson_oneTrailer() {
        String oneTrailerJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"quicktime\": [],\n" +
                "\"youtube\": [\n" +
                "{\n" +
                "\"name\": \"Official US Teaser Trailer\",\n" +
                "\"size\": \"HD\",\n" +
                "\"source\": \"c38r-SAnTWM\",\n" +
                "\"type\": \"Trailer\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(TrailerJsonUtils.extractResultsFromMovieTrailerJson(oneTrailerJson).size() == 1);
        Assert.assertEquals("c38r-SAnTWM",
                TrailerJsonUtils.extractResultsFromMovieTrailerJson(oneTrailerJson).get(0).getKeyString());
    }

    @Test
    public void testExtractResultsFromMovieTrailerJson_twoTrailers() {
        String twoTrailerJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"quicktime\": [],\n" +
                "\"youtube\": [\n" +
                "{\n" +
                "\"name\": \"Official US Teaser Trailer\",\n" +
                "\"size\": \"HD\",\n" +
                "\"source\": \"c38r-SAnTWM\",\n" +
                "\"type\": \"Trailer\"\n" +
                "},\n" +
                "{\n" +
                "\"name\": \"Lumiere Motion Poster\",\n" +
                "\"size\": \"HD\",\n" +
                "\"source\": \"bgeSXHvPoBI\",\n" +
                "\"type\": \"Clip\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(TrailerJsonUtils.extractResultsFromMovieTrailerJson(twoTrailerJson).size() == 2);
        Assert.assertEquals("c38r-SAnTWM",
                TrailerJsonUtils.extractResultsFromMovieTrailerJson(twoTrailerJson).get(0).getKeyString());
        Assert.assertEquals("bgeSXHvPoBI",
                TrailerJsonUtils.extractResultsFromMovieTrailerJson(twoTrailerJson).get(1).getKeyString());
    }
}
