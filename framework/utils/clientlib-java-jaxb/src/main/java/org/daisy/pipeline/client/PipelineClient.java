package org.daisy.pipeline.client;


import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Strings;

import org.daisy.pipeline.webservice.jaxb.base.Alive;
import org.daisy.pipeline.webservice.jaxb.clients.Client;
import org.daisy.pipeline.webservice.jaxb.clients.Clients;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobSizes;
import org.daisy.pipeline.webservice.jaxb.job.Jobs;
import org.daisy.pipeline.webservice.jaxb.queue.Queue;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.script.Scripts;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple but full-featured pipline2 WS client
 */
public class PipelineClient {

        private static final Logger logger = LoggerFactory.getLogger(PipelineClient.class);

        private WebTarget target;
        private String baseUri;

        public PipelineClient(String baseUri) {
                this.baseUri=baseUri;
                ClientConfig config=new ClientConfig();
                config.register(MultiPartFeature.class);
                this.target = ClientBuilder.newClient(config).target(baseUri);
        }
        public PipelineClient(String baseUri,String clientId,String secret) {
                this.baseUri=baseUri;
                this.target = ClientBuilder.newClient(new ClientConfig().register(new Authenticator(clientId,secret))).target(baseUri);
        }
        private <T> T get(String path,Class<T> result) {
                
                return target.path(path).request().get(result);

        }
        public Response get(String path) {
                
                return target.path(path).request().get();

        }
        private <T> T delete(String path,Class<T> result) {
                
                return target.path(path).request().delete(result);

        }
        private <T,U> U post(String path,T payload,Class<U> result) {
                
                return target.path(path).request().post(Entity.xml(payload),result);

        }
        private <T,U>  U postMultipart(String path,T payload, InputStream stream, Class<U> result) {
                FormDataMultiPart data=new FormDataMultiPart();
                data.field("job-data",stream,MediaType.APPLICATION_OCTET_STREAM_TYPE);
                FormDataBodyPart request=new FormDataBodyPart("job-request",payload,MediaType.APPLICATION_XML_TYPE);
                data.bodyPart(request);
                
                return target.path(path).request().post(Entity.entity(data,MediaType.MULTIPART_FORM_DATA_TYPE),result);

        }
        private <T,U> U put(String path,T payload,Class<U> result) {
                
                return target.path(path).request().put(Entity.xml(payload),result);

        }

        public Alive alive() {
                return this.get("alive",Alive.class);
        }
        public Jobs jobs() {
                return this.get("jobs",Jobs.class);
        }

        public Scripts scripts() {
                return this.get("scripts",Scripts.class);
                
        }

        public Job sendJob(JobRequest request) throws Exception{
                return this.post("jobs",request,Job.class);
        }
        public Job sendJob(JobRequest request,InputStream data) throws Exception{
                return this.postMultipart("jobs",request,data,Job.class);
        }

        public Job job(String id) throws Exception{
                return this.get(String.format("jobs/%s",id),Job.class);
        }
        public void delete(String id) throws Exception{
                this.delete(String.format("jobs/%s",id),Void.class);
        }

        public String log(String id){
                return this.get(String.format("jobs/%s/log",id),String.class);
        }

        public Queue queue() throws Exception{
                return this.get(String.format("queue"),Queue.class);
        }
        public Queue moveUp(String id) throws Exception{
                return this.get(String.format("queue/up/%s",id),Queue.class);
        }
        public Queue moveDown(String id) throws Exception{
                return this.get(String.format("queue/down/%s",id),Queue.class);
        }

        /// ADMIN calls
        public void halt(String key) {
                this.get(String.format("admin/halt/%s",key),Void.class);
        }

        public Clients clients() {
                return this.get(String.format("admin/clients"),Clients.class);
        }

        public Client addClient(Client client) throws Exception{
                return this.post(String.format("admin/clients"),client,Client.class);
        }

        public void deleteClient(String id) throws Exception{
                this.delete(String.format("admin/clients/%s",id),Void.class);
        }

        public Client updateClient(Client client) throws Exception{
                return this.put(String.format("admin/clients/%s",client.getId()),client,Client.class);
        }
        public Client client(String id) throws Exception{
                return this.get(String.format("admin/clients/%s",id),Client.class);
        }
        public JobSizes sizes() throws Exception{
                return this.get("admin/sizes",JobSizes.class);
        }

        /**
         * @return the baseUri
         */
        public String getBaseUri() {
                return baseUri;
        }

        private static class Authenticator implements ClientRequestFilter {
                private String clientId;
                private String secret;

                /**
                 * @param clientId
                 * @param secret
                 */
                public Authenticator(String clientId, String secret) {
                        this.clientId = clientId;
                        this.secret = secret;
                }

                @Override
                public void filter(ClientRequestContext ctxt) throws IOException {
                        String timestamp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
                        String nonce=new Integer(new Random().nextInt(1073741824)).toString();
                        nonce=Strings.padStart(nonce,30,'0');
                        
                        UriBuilder builder=UriBuilder.fromUri(ctxt.getUri());
                        builder.queryParam("authid",this.clientId);
                        builder.queryParam("time",timestamp);
                        builder.queryParam("nonce",nonce);
                        //logger.info("secret "+secret);
                        SecretKeySpec key = new SecretKeySpec((this.secret).getBytes("UTF-8"), "HmacSHA1");
                        try {
                                Mac mac = Mac.getInstance("HmacSHA1");
                                mac.init(key);

                                byte[] bytes = mac.doFinal(builder.clone().build()
                                                .toString().getBytes("UTF-8"));

                                builder.queryParam("sign", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
                                ctxt.setUri(builder.build());
                        } catch (Exception e) {
                                logger.warn(e.getMessage());
                                throw new RuntimeException(e);
                        }




                }
                
        }
}


