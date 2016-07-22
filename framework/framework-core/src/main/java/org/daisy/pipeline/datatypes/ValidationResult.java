package org.daisy.pipeline.datatypes;

import com.google.common.base.Optional;

public class ValidationResult {
        
        private boolean valid;
        private Optional<String> message;

        /**
         * @param valid
         */
        private ValidationResult(boolean valid) {
                this(valid,Optional.fromNullable((String)null));
        }

        /**
         * @param valid
         * @param message
         */
        private ValidationResult(boolean valid, Optional<String> message) {
                this.valid = valid;
                this.message = message;
        }

        public static ValidationResult valid(){
                return new ValidationResult(true);
        }
        public static ValidationResult notValid(String reason){
                return new ValidationResult(false,Optional.of(reason));
        }

        /**
         * @return the valid
         */
        public boolean isValid() {
                return valid;
        }

        /**
         * @return the message
         */
        public Optional<String> getMessage() {
                return message;
        }

}
