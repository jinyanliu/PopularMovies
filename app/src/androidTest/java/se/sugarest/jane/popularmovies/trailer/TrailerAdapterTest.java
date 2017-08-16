package se.sugarest.jane.popularmovies.trailer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 17-8-15.
 */

@RunWith(AndroidJUnit4.class)
public class TrailerAdapterTest {

    private Context instrumentationCtx;

    @Before
    public void setUp() {
        instrumentationCtx = InstrumentationRegistry.getContext();
    }

    List<Trailer> trailerData = new ArrayList<>();

    TrailerAdapter trailerAdapter = new TrailerAdapter(null, instrumentationCtx);

    /***********************************************************************************************
     *                                                                                             *
     * trailerData == null happens when trailerData hasn't been loaded yet, it won't               *
     * trigger trailerAdapter.setTrailerData(trailerData) method.                                  *
     * The getItemCountTest_noTrailerData() method is testing the situation that                   *
     * trailerData != null but trailerData.size == 0,                                              *
     * which means the trailerData has already been loaded, but the movie doesn't have trailers.   *
     * So we don't initialize trailerData, just pass it to setTrailerData(trailerData),            *
     * meaning no trailers available.                                                              *
     *                                                                                             *
     ***********************************************************************************************/

    @Test
    public void getItemCountTest_nullTrailerData() throws Exception {
        // trailerData == null;
        Assert.assertEquals(0, trailerAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_noTrailerData() throws Exception {
        trailerAdapter.setTrailerData(trailerData);
        Assert.assertEquals(0, trailerAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_oneTrailer() throws Exception {
        String oneTrailerKey = "c38r-SAnTWM";
        Trailer trailer = new Trailer(oneTrailerKey);
        trailerData.add(trailer);
        trailerAdapter.setTrailerData(trailerData);
        Assert.assertEquals(1, trailerAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_twoTrailer() throws Exception {
        String oneTrailerKey = "c38r-SAnTWM";
        String secondTrailerKey = "bgeSXHvPoBI";
        Trailer trailerOne = new Trailer(oneTrailerKey);
        Trailer trailerTwo = new Trailer(secondTrailerKey);
        trailerData.add(trailerOne);
        trailerData.add(trailerTwo);
        trailerAdapter.setTrailerData(trailerData);
        Assert.assertEquals(2, trailerAdapter.getItemCount());
    }
}