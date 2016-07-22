package org.daisy.common.fuzzy;

import org.daisy.common.fuzzy.FuzzySet;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;

public class FuzzySetTest   {

        @Test
        public void getMembership() {
                FuzzySet set = new FuzzySet("Test",10, new Function<Double, Double>() {
                        @Override
                        public Double apply(Double arg0) {

                                return arg0-1.0;
                        }
                });

                Assert.assertEquals("Testing function",1.0,set.getMembership().apply(2.0),0.0);
        }

}
