package org.daisy.pipeline.webservice.restlet;

import com.google.common.base.Optional;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.webservice.Authenticator;
import org.daisy.pipeline.webservice.PipelineWebServiceConfiguration;

import org.restlet.data.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthenticatedResource extends GenericResource {

        private static Logger logger = LoggerFactory.getLogger(Authenticator.class.getName());
        private boolean isAuthenticated = false;
        private Client client;

        @Override
        public void doInit() {
                super.doInit();
                client = authenticate(getReference().toString(), getQuery());
                isAuthenticated = (client != null);
        }

        private Client authenticate(String uri, Form query) {
                if (getConfiguration().isAuthenticationEnabled() == false) {
                        // if authentication is not enabled, then all requests can be
                        // considered automatically authenticated
                        return getStorage().getClientStorage().defaultClient();
                }
                String authid = query.getFirstValue("authid");
                Optional<Client> client = getStorage().getClientStorage().get(authid);
                // make sure the client exists
                if (!client.isPresent()) {
                        logger.error(String.format("Client with auth ID '%s' not found", authid));
                        return null;
                }
                if (new Authenticator(getStorage().getRequestLog())
                    .authenticate(client.get(),
                                  query.getFirstValue("sign"),
                                  query.getFirstValue("time"),
                                  query.getFirstValue("nonce"),
                                  uri,
                                  getConfiguration().getMaxRequestTime()))
                        return client.get();
                else
                        return null;
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
