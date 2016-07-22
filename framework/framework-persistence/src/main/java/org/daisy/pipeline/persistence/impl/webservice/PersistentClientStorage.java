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

        //This client is the default client for "non client-aware" uses of the api
        final static PersistentClient DEFAULT = new PersistentClient(
                        "DEFAULT_PERSISTENT_CLIENT_1685216", "", Role.ADMIN, "",
                        Priority.MEDIUM);

        private Database database;

        public PersistentClientStorage(Database database) {
                this.database = database;
        }

        public void setDatabase(Database database) {
                this.database = database;
        }

        @Override
        public List<? extends Client> getAll() {
                //ignore default
                return database.runQuery(String.format("select c from PersistentClient as c where c.id <>'%s'",DEFAULT.getId()),
                                PersistentClient.class);
        }

        @Override
        public Optional<Client> get(String id) {
                //get null?
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
                //check if it's default
                if(DEFAULT.getId().equals(id)){
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
                //no such client
                if(!optClient.isPresent()){
                        return optClient;
                }


                if(optClient.get().getId().equals(DEFAULT.getId())){
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
                //try and get the client from the db
                Optional<Client> def = this.get(DEFAULT.getId());
                if (!def.isPresent()) {
                        this.database.addObject(DEFAULT);
                }
                return DEFAULT;
        }


}
