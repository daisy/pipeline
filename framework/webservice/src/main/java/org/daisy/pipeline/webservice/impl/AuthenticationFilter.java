package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

import javax.servlet.annotation.WebFilter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.webservice.Authenticator;
import org.daisy.pipeline.webservice.PipelineWebServiceConfiguration;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class.getName());

	private final String protocol;
	private final PipelineWebServiceConfiguration configuration;
	private final WebserviceStorage storage;

	public AuthenticationFilter(String protocol,
	                            PipelineWebServiceConfiguration configuration,
	                            WebserviceStorage storage) {
		this.protocol = protocol;
		this.configuration = configuration;
		this.storage = storage;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest,
	                     ServletResponse servletResponse,
	                     FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		Client client = authenticate(request);
		if (client != null)
			filterChain.doFilter(new AuthenticatedRequest(request, client),
			                     servletResponse);
		else {
			HttpServletResponse response = (HttpServletResponse)servletResponse;
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failed");
		}
	}

	@Override
	public void destroy() {
	}

	private static class AuthenticatedRequest extends HttpServletRequestWrapper {

		private final ClientPrincipal client;

		AuthenticatedRequest(HttpServletRequest request, Client client) {
			super(request);
			this.client = new ClientPrincipal(client);
		}

		@Override
		public ClientPrincipal getUserPrincipal() {
			return client;
		}
	}

	public static class ClientPrincipal implements Principal {

		private final Client client;

		private ClientPrincipal(Client client) {
			this.client = client;
		}

		public Client getClient() {
			return client;
		}

		@Override
		public String getName() {
			return client.getId();
		}

		@Override
		public String toString() {
			return client.toString();
		}
	}

	private Client authenticate(HttpServletRequest request) {
		if (configuration.isAuthenticationEnabled() == false)
			return storage.getClientStorage().defaultClient();
		String authid = request.getParameter("authid");
		Optional<Client> client = storage.getClientStorage().get(authid);
		// make sure the client exists
		if (!client.isPresent()) {
			logger.error(String.format("Client with auth ID '%s' not found", authid));
			return null;
		}
		if (new Authenticator(storage.getRequestLog())
		    .authenticate(client.get(),
		                  request.getParameter("sign"),
		                  request.getParameter("time"),
		                  request.getParameter("nonce"),
		                  // using protocol because request.getScheme() returns "http", not "ws"
		                  protocol + "://" + request.getServerName()
		                           + ":" + request.getServerPort()
		                           + request.getRequestURI()
		                           + "?" + request.getQueryString(),
		                  configuration.getMaxRequestTime()))
			return client.get();
		else
			return null;
	}
}
