package org.daisy.pipeline.persistence.impl.webservice;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;

@Entity
// @NoSql(dataFormat=DataFormatType.MAPPED)
public class PersistentClient implements Client {

        public static final String MODEL_CLIENT_ID="id";
        @Id
        private String id;
        private String secret;
        private Role role ;

        // in the future, use a separate table to list contact information for
        // client app maintainers
        // with a single field, we'll just store email info
        private String contactInfo;

        //client's priority

	@Enumerated(EnumType.ORDINAL)
        private Priority priority;

        public PersistentClient() {
        }

        public PersistentClient(String id, String secret, Role role, String contactInfo,Priority priority) {
                this.id = id;
                this.secret = secret;
                this.role = role;
                this.contactInfo = contactInfo;
                this.priority=priority;
        }

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getSecret() {
                return secret;
        }

        public void setSecret(String secret) {
                this.secret = secret;
        }

        public Role getRole() {
                return role;
        }

        public void setRole(Role role) {
                this.role = role;
        }

        public String getContactInfo() {
                return contactInfo;
        }

        public void setContactInfo(String contactInfo) {
                this.contactInfo = contactInfo;
        }

        @Override
        public Priority getPriority() {
                return this.priority;
        }

        /**
         * @param priority the priority to set
         */
        public void setPriority(Priority priority) {
                this.priority = priority;
        }

}
