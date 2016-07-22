package org.daisy.common.fuzzy;

import org.daisy.common.fuzzy.MembershipFunctions.TrapezoidFunction;
import org.junit.Assert;
import org.junit.Test;

public class MembershipFunctionsTest   {

        @Test(expected=IllegalArgumentException.class)
        public void newBadBoundariesOrderTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.5,0.3,0.4,0.7);
        }

        @Test
        public void inSegmentOutTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                Assert.assertEquals("Out of lower boundary 0.1",-1,set.getSegment(0.1));
                Assert.assertEquals("Out of upper boundary 0.9",-1,set.getSegment(0.9));
        }
        @Test
        public void inSegmentLowerThanZero(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.0,0.0,0.1,0.8);
                Assert.assertEquals("lower than zero",0.0,set.getSegment(-0.1),0.0);
        }


        @Test
        public void inSegmentGreaterThanOne(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.5,0.6,1.0,1.0);
                Assert.assertEquals(">1",1.0,set.getSegment(7.0),0.0);
        }

        @Test
        public void applyInLowerBoundary(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.0,0.0,0.1,0.8);
                Assert.assertEquals("apply in lower boundary ",1.0,set.apply(0.0),0.0);
        }
        @Test
        public void applyInUpperBoundary(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.5,0.7,1.0,1.0);
                Assert.assertEquals("apply in upper boundary ",1.0,set.apply(1.0),0.0);
        }
        @Test
        public void applyOutOfSetTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                Assert.assertEquals("membership 0, out of lower ",0.0,set.apply(0.1),0.0);
                Assert.assertEquals("memebership 0, out of upper0.9",0.0,set.apply(0.9),0.0);
        }


        @Test
        public void inSegmentTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                Assert.assertEquals("seg 0",0,set.getSegment(0.3));
                Assert.assertEquals("seg 1",1,set.getSegment(0.65));
                Assert.assertEquals("seg 2",2,set.getSegment(0.75));
        }

        @Test
        public void applyFirstSegTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                //(x-0.2)/(0.6-0.2)=0.25
                Assert.assertEquals("Slope",0.25,set.apply(0.3),0.01);
        }

        @Test
        public void applySecondSegTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                //(x-0.2)/(0.6-0.2)=0.25
                Assert.assertEquals("Slope",1.0,set.apply(0.65),0.0);
        }

        @Test
        public void applyThirdSegTrapezoid(){
                TrapezoidFunction set=TrapezoidFunction.fromPoints(0.2,0.6,0.7,0.8);
                //(0.8-0.72)/(0.8-0.7)=0.25
                Assert.assertEquals("Slope",0.8,set.apply(0.72),0.0);
        }
        
}
