package org.daisy.pipeline.webservice.impl;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class VolatileClientStorageTest   {

        VolatileClientStorage storage;  
        String secret="secret";
        String contact="name@server.com";
        @Before
        public void setUp(){
                storage=new VolatileClientStorage();

        }
        @Test
        public void listEmpty(){
                Assert.assertEquals("Empty",0,storage.getAll().size());

        }

        @Test
        public void listAll(){
                String id="IDDD";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
                c=storage.addClient(id+"2",secret,Role.ADMIN,contact,Priority.HIGH);
                Assert.assertEquals("Empty",2,storage.getAll().size());

        }

        @Test
        public void listWithDefault(){
                Client c= storage.defaultClient();
                Assert.assertEquals("Default should not be in the list",0,storage.getAll().size());
        }

        @Test
        public void getOk(){
                String id="id";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
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
                boolean deleted=this.storage.delete(def.getId());
                Assert.assertFalse("default can't be deleted",deleted);
        }

        @Test
        public void update(){
                String id="ID";
                String otherSecret="otherSecret";
                String otherContact="otherContact";
                Optional<Client> c=storage.addClient(id,secret,Role.ADMIN,contact,Priority.HIGH);
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
                Optional<Client>def=storage.update(VolatileClientStorage.DEFAULT.getId(),"",Role.ADMIN,"",Priority.LOW);
                Assert.assertFalse(def.isPresent());
        }

        @Test
        public void addClientDefaultPriority(){
                String id="cli"; 
                Optional<Client>c =storage.addClient(id,secret,Role.ADMIN,contact,Priority.MEDIUM);
                Optional<Client> res=storage.get(id);
                Assert.assertTrue(res.isPresent());
                Assert.assertEquals("priority",Priority.MEDIUM,res.get().getPriority());
        }




}
