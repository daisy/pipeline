package org.daisy.pipeline.persistence.impl.webservice;

import java.util.List;

import javax.persistence.NoResultException;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.persistence.impl.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class PersistentClientStorage implements ClientStorage {

        private static Logger logger = LoggerFactory
                .getLogger(PersistentClientStorage.class);

        private PersistentClient defaultClient = null;

        private Database database;

        public PersistentClientStorage(Database database) {
                this.database = database;
        }

        @Override
        public List<? extends Client> getAll() {
                // ignore default client
                return database.runQuery(
                        String.format("select c from PersistentClient as c where c.id <>'%s'", Client.DEFAULT_ADMIN.getId()),
                        PersistentClient.class);
        }

        @Override
        public Optional<Client> get(String id) {
                String q = String.format(
                                "select c from PersistentClient as c where c.id='%s'", id);
                try {
                        return Optional.of((Client)database.getFirst(q, PersistentClient.class));
                } catch (NoResultException e) {
                        logger.debug(String.format("Client with id %s not found",id));
                        return Optional.absent();
                }
        }

        @Override
        public boolean delete(String id) {
                // don't delete the default client
                if (Client.DEFAULT_ADMIN.getId().equals(id)) {
                        return false;
                }
                Optional<Client>clientInDb = get(id);
                if (!clientInDb.isPresent()) {
                        return false;
                }
                return database.deleteObject(clientInDb.get());
        }

        @Override
        public Optional<Client> update(String id,String secret, Role role,
                        String contactInfo, Priority priority) {
                Optional<Client> optClient=this.get(id);
                if(!optClient.isPresent()){
                        return optClient;
                }
                // don't update default client
                if (optClient.get().getId().equals(Client.DEFAULT_ADMIN.getId())) {
                        return Optional.absent();
                }
                PersistentClient pClient=(PersistentClient)optClient.get(); 
                pClient.setSecret(secret);
                pClient.setRole(role);
                pClient.setContactInfo(contactInfo);
                pClient.setPriority(priority);
                database.updateObject(pClient);
                return Optional.of((Client)pClient);
        }

        @Override
        public Optional<Client> addClient(String id, String secret, Role role,
                        String contactInfo, Priority priority) {
                Preconditions.checkArgument(id!=null,"Client id can't be null");
                Optional<Client>client = get(id);
                if (client.isPresent()) {
                        logger.error(String.format("ID %s is already in use.", id));
                        return Optional.absent();
                }
                PersistentClient newClient= new PersistentClient(id, secret, role, contactInfo, priority);
                database.addObject(newClient);
                return Optional.of((Client)newClient);
        }

        @Override
        public Optional<Client> addClient(String id, String secret, Role role,
                        String contactInfo) {
                return addClient(id, secret, role, contactInfo, Priority.MEDIUM);
        }

        @Override
        public Client defaultClient() {
                // add client to database if needed
                if (defaultClient == null) {
                        defaultClient = new PersistentClient(Client.DEFAULT_ADMIN);
                        if (!get(defaultClient.getId()).isPresent()) {
                                database.addObject(defaultClient);
                        }
                }
                return defaultClient;
        }
}
