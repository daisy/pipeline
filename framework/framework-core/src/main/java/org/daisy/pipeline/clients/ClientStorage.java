package org.daisy.pipeline.clients;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client.Role;

import com.google.common.base.Optional;

/**
 * Interface for creating, deleting and accessing clients
 *
 */
public interface  ClientStorage {
        /**
         * Gets all the clients in the system. The default client is not listed.
         * @param id
         * @return
         */
        public List<? extends Client> getAll();

        /**
         * Get the client idetified by the id.
         *
         * @param id
         * @return
         */
        public Optional<Client> get(String id);

        /**
         * Removes the client, the default client can't be deleted.
         *
         * @param client
         * @return
         */
        public boolean delete(String id);

        /**
         * Updates the client using the new values. Returns the updated client or absent if the client couldn't 
         * be loaded.
         * @param client
         * @return
         */
        public Optional<Client> update(String id, String secret, Role role, String contactInfo,Priority priority);

        /**
         * Adds a new client to the storage using the provided details. Returns the created client or absent if the client 
         * couldn't be created.
         * @param id
         * @param secret
         * @param role
         * @param contactInfo
         * @param priority
         * @return
         */
        public Optional<Client> addClient(String id, String secret, Role role, String contactInfo,Priority priority);

        /**
         * Adds a new client to the storage using the provided details and the default priority. Returns the created client or absent if the client 
         * couldn't be created.
         * @param id
         * @param secret
         * @param role
         * @param contactInfo
         * @param priority
         * @return
         */
        public Optional<Client> addClient(String id, String secret, Role role, String contactInfo);

        /**
         * Returns the default client
         * @param id
         * @param secret
         * @param role
         * @param contactInfo
         * @param priority
         * @return
         */
        public Client defaultClient();

}
