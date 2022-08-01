package org.daisy.pipeline.webservice.jetty.impl;

import java.lang.reflect.Field;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.eclipse.jetty.websocket.jsr356.server.JsrHandshakeResponse;

import org.restlet.data.Status;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.impl.JobsWebSocketEndpoint;
import org.daisy.pipeline.webservice.impl.AuthenticationFilter;
import org.daisy.pipeline.webservice.PipelineWebServiceConfiguration;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.impl.AuthenticationFilter.ClientPrincipal;

public class WebSocketServer {

	private final PipelineWebServiceConfiguration configuration;
	private final WebserviceStorage storage;
	private final JobManagerFactory jobManagerFactory;
	private final CallbackHandler callbackHandler;
	private Server server;
	private ServerConnector connector;

	public WebSocketServer(PipelineWebServiceConfiguration configuration,
	                       WebserviceStorage storage,
	                       JobManagerFactory jobManagerFactory,
	                       CallbackHandler callbackHandler) {
		this.configuration = configuration;
		this.storage = storage;
		this.callbackHandler = callbackHandler;
		this.jobManagerFactory = jobManagerFactory;
	}

	public void start() throws Exception {
		server = new Server();
		connector = new ServerConnector(server);
		Routes routes = new Routes();
		int port = routes.getWebSocketPort();
		if (port > 0)
			connector.setPort(port);
		server.setConnectors(new Connector[] {connector});
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.setContextPath(routes.getPath());
		handler.addFilter(
			new FilterHolder(new AuthenticationFilter("ws", configuration, storage)),
			"/*", EnumSet.of(DispatcherType.REQUEST));
		server.setHandler(handler);
		WebSocketServerContainerInitializer.configure(
			handler,
			(servletContext, serverContainer) -> {
				serverContainer.setDefaultMaxTextMessageBufferSize(65535);
				serverContainer.addEndpoint(
					ServerEndpointConfig.Builder.create(JobsWebSocketEndpoint.class, Routes.JOB_ROUTE)
						.configurator(
							new ServerEndpointConfig.Configurator() {
								@Override
								public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
									if (endpointClass == JobsWebSocketEndpoint.class)
										return (T)(new JobsWebSocketEndpoint(jobManagerFactory,
										                                     callbackHandler,
										                                     routes));
									else
										return super.getEndpointInstance(endpointClass);
								}
								@Override
								public void modifyHandshake(ServerEndpointConfig sec,
								                            HandshakeRequest request,
								                            HandshakeResponse response) {
									String path = request.getRequestURI().getPath();
									String jobId = path.substring(path.lastIndexOf('/') + 1);
									Client client = ((ClientPrincipal)request.getUserPrincipal()).getClient();
									//sec.getUserProperties().put(Client.class.getName(), client);
									if (!jobManagerFactory.createFor(client).getJob(JobIdFactory.newIdFromString(jobId)).isPresent()) {
										// see https://stackoverflow.com/questions/53405281/websocket-pathparam-validation
										UpgradeResponse r = toUpgradeResponse((JsrHandshakeResponse)response);
										// FIXME: the code below doesn't work: the JobsWebSocketEndPoint.onOpen() method is still called
										r.setSuccess(false);
										r.setStatusCode(Status.CLIENT_ERROR_NOT_FOUND.getCode()); // 404
										// r.setStatusReason("Job not found"); // UnsupportedOperationException: Servlet's do not support Status Reason
										r.getHeaders().clear();
									}
								}
							})
						.build()); });
		server.start();
	}

	public void stop() throws Exception {
		server.stop();
	}

	public int getActualPort() {
		return connector.getLocalPort();
	}

	private static UpgradeResponse toUpgradeResponse(JsrHandshakeResponse response) {
		try {
			Field f = JsrHandshakeResponse.class.getDeclaredField("response");
			f.setAccessible(true);
			return (UpgradeResponse)f.get(response);
		} catch (Throwable e) {
			throw new RuntimeException(); // should not happen
		}
	}
}
