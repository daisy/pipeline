package org.daisy.pipeline.webservice.impl;

import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import org.daisy.pipeline.webservice.Routes;

class WebSocketServer {

	private final PipelineWebService webservice;
	private Server server;
	private ServerConnector connector;

	public WebSocketServer(PipelineWebService webservice) {
		this.webservice = webservice;
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
		server.setHandler(handler);
		WebSocketServerContainerInitializer.configure(
			handler,
			(servletContext, serverContainer) -> {
				serverContainer.setDefaultMaxTextMessageBufferSize(65535);
				serverContainer.addEndpoint(
					ServerEndpointConfig.Builder.create(JobsEndpoint.class, Routes.JOB_ROUTE)
						.configurator(
							new ServerEndpointConfig.Configurator() {
								@Override
								public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
									if (endpointClass == JobsEndpoint.class)
										return (T)(new JobsEndpoint(webservice));
									else
										return super.getEndpointInstance(endpointClass); }})
						.build()); });
		server.start();
	}

	public void stop() throws Exception {
		server.stop();
	}

	public int getActualPort() {
		return connector.getLocalPort();
	}
}
