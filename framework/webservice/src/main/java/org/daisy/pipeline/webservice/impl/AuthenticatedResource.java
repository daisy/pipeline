package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.webservice.Authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public abstract class AuthenticatedResource extends GenericResource {
        private static Logger logger = LoggerFactory.getLogger(Authenticator.class
                        .getName());
        private boolean isAuthenticated = false;
        private Client client;

        @Override
        public void doInit() {
                super.doInit();
                if (webservice().getConfiguration().isAuthenticationEnabled() == false) {
                        // if authentication is not enabled, then all requests can be considered automatically authenticated
                        client=webservice().getStorage().getClientStorage().defaultClient();
                        isAuthenticated = true;
                } else {
                        isAuthenticated = authenticate();
                }
        }

        private boolean authenticate() {

                long maxRequestTime = webservice().getConfiguration()
                                .getMaxRequestTime();
                String authid = getQuery().getFirstValue("authid");
                Optional<Client> optionalClient= webservice().getStorage().getClientStorage().get(authid);
                // make sure the client exists
                if (!optionalClient.isPresent()) {
                        logger.error(String.format("Client with auth ID '%s' not found", authid));
                        return false;
                }
                this.client=optionalClient.get();
                RequestLog requestLog = webservice().getStorage().getRequestLog();
                return new Authenticator(requestLog).authenticate(
                        this.client,
                        getQuery().getFirstValue("sign"),
                        getQuery().getFirstValue("urlsign"),
                        getQuery().getFirstValue("time"),
                        getQuery().getFirstValue("nonce"),
                        getReference().toString(),
                        maxRequestTime
                );
        }

        public boolean isAuthenticated() {
                return isAuthenticated;
        }

        /**
         * @return the client
         */
        public Client getClient() {
                return client;
        }

}
