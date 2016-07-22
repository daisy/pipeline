package org.daisy.common.priority;

import org.daisy.common.priority.Priority;
import org.junit.Assert;
import org.junit.Test;

public class PriorityTest   {
        
        @Test
        public void asDouble(){
                Assert.assertEquals("High",1.0,Priority.HIGH.asDouble(),0.0);
                Assert.assertEquals("Medium",0.5,Priority.MEDIUM.asDouble(),0.0);
                Assert.assertEquals("Low",0.0,Priority.LOW.asDouble(),0.0);
        }
}
