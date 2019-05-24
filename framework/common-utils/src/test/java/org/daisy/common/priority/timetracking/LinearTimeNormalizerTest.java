package org.daisy.common.priority.timetracking;

import org.daisy.common.priority.timetracking.LinearTimeNormalizer;
import org.daisy.common.priority.timetracking.TimeStats;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class LinearTimeNormalizerTest   {
        @Mock LinearTimeNormalizer normaliser;

        @Test
        public void normalize(){
                TimeStats stats= new TimeStats(200,new long[]{100,200});
                Function<Long,Double> func=(new LinearTimeNormalizer()).getFunction(stats);
                Assert.assertEquals("Normalize min",0.0,func.apply(200L),0.0);
                Assert.assertEquals("Normalize medium",0.5,func.apply(150L),0.0);
                Assert.assertEquals("Normalize max",1.0,func.apply(100L),0.0);

        }

        @Test
        public void findMinMax(){

                long vals[] = new long[]{3,5,1000,0,123};

                Mockito.doCallRealMethod().when(normaliser).findMinMax(vals);
                Assert.assertArrayEquals("Find minum and maximum", new long[]{0,1000},normaliser.findMinMax(vals));

        }
        
}
