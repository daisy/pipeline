package org.daisy.pipeline.datatypes;

import org.junit.Assert;
import org.junit.Test;

public class ValidationResultTest {

        @Test
        public void testValid() throws Exception {
            ValidationResult valid=ValidationResult.valid();
            Assert.assertTrue("The result should be valid",valid.isValid());
            Assert.assertFalse("The message should not be present",valid.getMessage().isPresent());
        }

        @Test
        public void testNotValid() throws Exception {
            ValidationResult valid=ValidationResult.notValid("because");
            Assert.assertFalse("The result should not be valid",valid.isValid());
            Assert.assertTrue("The message should be present",valid.getMessage().isPresent());
            Assert.assertEquals("The message should be 'because'","because",valid.getMessage().get());
        }
        
}
