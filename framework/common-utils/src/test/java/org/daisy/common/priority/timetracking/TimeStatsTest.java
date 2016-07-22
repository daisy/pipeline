package org.daisy.common.priority.timetracking;

import org.daisy.common.priority.timetracking.TimeStats;
import org.junit.Assert;
import org.junit.Test;

public class TimeStatsTest   {
       
        @Test
        public void referrencedTimes(){
                long[] rawTimes= new long[]{0,1,2};
                long[] referencedTimes= new long[]{10,9,8};
                long reference=10;

                TimeStats timeStats= new TimeStats(reference,rawTimes);
                long[] res=timeStats.getReferencedTimes();
                Assert.assertArrayEquals("Checking referenced times",referencedTimes,res);
        }
}
