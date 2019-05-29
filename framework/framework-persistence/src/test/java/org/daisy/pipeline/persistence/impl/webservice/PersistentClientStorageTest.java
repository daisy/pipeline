package org.daisy.pipeline.persistence.impl.webservice;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.DatabaseProvider;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClientStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;


@RunWith(MockitoJUnitRunner.class)
public class PersistentClientStorageTest {

        Database db;
        PersistentClientStorage storage;
        String secret="secret";
        String contact="name@server.com";

        List<Client> toDel;
        @Before
        public void setUp(){
                db=Mockito.spy(DatabaseProvider.getDatabase());
                storage=new PersistentClientStorage(db);
                toDel= Lists.newLinkedList();
        }

        @After
        public void tearDown(){
                for(Client c:toDel){
                        db.deleteObject(c);
                }
        }

        @Test
        public void listEmpty(){
                Assert.assertEquals("Empty",0,storage.getAll().size());

        }

        @Test
        public void listAll(){
                String id="IDDD";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                toDel.add(c.get());
                c=storage.addClient(id+"2",secret,Role.ADMIN,contact,Priority.HIGH);
                toDel.add(c.get());
                Assert.assertEquals("Empty",2,storage.getAll().size());

        }

        @Test
        public void listWithDefault(){
                Client c= storage.defaultClient();
                toDel.add(c);
                Assert.assertEquals("Default should not be in the list",0,storage.getAll().size());

        }

        @Test
        public void getOk(){
                String id="id";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                toDel.add(c.get());
                Client cli=storage.get(c.get().getId()).get();
                Assert.assertEquals("what I put is what I get", id,cli.getId());

        }


        @Test
        public void getNull(){
                String id="id";
                Optional<Client> c = storage.get(null);
                Assert.assertFalse("Get null", c.isPresent());

        }

        @Test
        public void delete(){
                String id="id";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                toDel.add(c.get());
                boolean del=storage.delete(id);
                Assert.assertTrue("Was deleted", del);
                c=storage.get(id);
                Assert.assertFalse("Is not in the storage",c.isPresent());
        }

        @Test
        public void deleteNonExisiting(){
                boolean del=storage.delete("non-existing id");
                Assert.assertFalse(del);
        }

        @Test
        public void deleteNull(){
                boolean del=storage.delete(null);
                Assert.assertFalse(del);
        }
        @Test
        public void deleteDefault(){
                Client def=storage.defaultClient();
                this.toDel.add(def);
                boolean deleted=this.storage.delete(def.getId());
                Assert.assertFalse("default can't be deleted",deleted);
        }

        @Test
        public void update(){
                String id="ID";
                String otherSecret="otherSecret";
                String otherContact="otherContact";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                toDel.add(c.get());
                c=this.storage.update(id,otherSecret,Role.CLIENTAPP,otherContact,Priority.LOW);
                Assert.assertEquals("updated secret",otherSecret,c.get().getSecret());
                Assert.assertEquals("updated contact",otherContact,c.get().getContactInfo());
                Assert.assertEquals("updated role",Role.CLIENTAPP,c.get().getRole());
                Assert.assertEquals("updated priority",Priority.LOW,c.get().getPriority());
        }

        @Test
        public void updateNoneExisting(){
                String id="ID";
                String otherSecret="otherSecret";
                String otherContact="otherContact";
                Optional<Client> c=this.storage.update(id,otherSecret,Role.CLIENTAPP,otherContact,Priority.LOW);
                Assert.assertFalse("Update non-exisiting client",c.isPresent());
        }

        @Test
        public void updateNull(){
                String otherSecret="otherSecret";
                String otherContact="otherContact";
                Optional<Client> c=this.storage.update(null,otherSecret,Role.CLIENTAPP,otherContact,Priority.LOW);
                Assert.assertFalse("Update null client",c.isPresent());
        }

        @Test
        public void addClient(){
                String id="cli"; 
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                Assert.assertTrue(c.isPresent());
                toDel.add(c.get());
                Client res=storage.get(id).get();
                Assert.assertEquals("id",id,res.getId());
                Assert.assertEquals("secret",secret,res.getSecret());
                Assert.assertEquals("contact",contact,res.getContactInfo());
                Assert.assertEquals("role",Role.ADMIN,res.getRole());
                Assert.assertEquals("priority",Priority.HIGH,res.getPriority());
        }

        @Test(expected=IllegalArgumentException.class)
        public void addNullClient(){
                Optional<Client> c=storage.addClient(null,secret,Role.ADMIN,contact,Priority.HIGH);
        }

        @Test 
        public void addDefault(){
                Client c= storage.defaultClient();
                Optional<Client>def=storage.update(PersistentClientStorage.DEFAULT.getId(),"",Role.ADMIN,"",Priority.LOW);
                //make sure is in the db
                toDel.add(c);
                Assert.assertFalse(def.isPresent());
        }
        @Test
        public void addClientDefaultPriority(){
                String id="cli"; 
                Optional<Client>c =storage.addClient(id,secret,Role.ADMIN,contact,Priority.MEDIUM);
                toDel.add(c.get());
                Optional<Client> res=storage.get(id);
                Assert.assertTrue(res.isPresent());
                Assert.assertEquals("priority",Priority.MEDIUM,res.get().getPriority());
        }

        @Test
        public void getDefaultClientEmpty(){
                Client def= storage.defaultClient();
                toDel.add(def);
                //the client has been inserted in the db
                Mockito.verify(db,Mockito.times(1)).addObject(Mockito.any());
                Assert.assertEquals("Check default id",PersistentClientStorage.DEFAULT.getId(),def.getId());
        }

        @Test
        public void getDefaultClientTwice(){
                Client def= storage.defaultClient();
                toDel.add(def);
                //the client has been inserted in the db but only once
                Mockito.verify(db,Mockito.times(1)).addObject(Mockito.any());
                def= storage.defaultClient();
                Mockito.verify(db,Mockito.times(1)).addObject(Mockito.any());
                Assert.assertEquals("Check default id",PersistentClientStorage.DEFAULT.getId(),def.getId());
        }
}
