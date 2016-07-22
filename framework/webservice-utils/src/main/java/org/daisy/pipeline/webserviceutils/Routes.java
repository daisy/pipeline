package org.daisy.pipeline.webserviceutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Routes.class.getName());

	public static final String SCRIPTS_ROUTE = "/scripts";
	public static final String SCRIPT_ROUTE = "/scripts/{id}";
	public static final String JOBS_ROUTE = "/jobs";
	public static final String JOB_ROUTE = "/jobs/{id}";
	public static final String LOG_ROUTE = "/jobs/{id}/log";
	public static final String JOB_CONF_ROUTE = "/jobs/{id}/configuration";
	public static final String ALIVE_ROUTE = "/alive";
	public static final String RESULT_ROUTE = "/jobs/{id}/result";
	public static final String RESULT_OPTION_ROUTE = "/jobs/{id}/result/option/{name}";
	public static final String RESULT_OPTION_ROUTE_IDX = "/jobs/{id}/result/option/{name}/idx/{idx}";
	public static final String RESULT_PORT_ROUTE= "/jobs/{id}/result/port/{name}";
	public static final String RESULT_PORT_ROUTE_IDX = "/jobs/{id}/result/port/{name}/idx/{idx}";
	public static final String HALT_ROUTE = "/admin/halt/{key}";
	public static final String CLIENTS_ROUTE = "/admin/clients";
	public static final String CLIENT_ROUTE = "/admin/clients/{id}";
	public static final String PROPERTIES_ROUTE = "/admin/properties";
	public static final String SIZES_ROUTE = "/admin/sizes";
	public static final String QUEUE_ROUTE= "/queue";
	public static final String QUEUE_UP_ROUTE= "/queue/up/{jobId}";
	public static final String QUEUE_DOWN_ROUTE= "/queue/down/{jobId}";
	public static final String DATATYPE_ROUTE= "/datatypes/{id}";
	public static final String DATATYPES_ROUTE= "/datatypes";
	public static final String BATCH_ROUTE= "/batch/{id}";



	private String path = "/ws";
	private static final int PORT=8181;
	private int portNumber = 0;
	private String host = "localhost";
	private String proto="http://";

	public Routes() {
		readOptions();
	}

	public String getHost() {
		return host;
	}
	public String getPath() {
		return path;
	}
	public int getPort() {
		return portNumber;
	}
	public String getBaseUri() {
		return proto+host + ":" + String.valueOf(portNumber) + path;
	}

	private void readOptions() {
		String path = System.getProperty(Properties.PATH);
		if (path != null) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			this.path = path;
		}

		String hostname = System.getProperty(Properties.HOST);
		if (hostname != null) {
			host = hostname;
		}
		if (System.getProperty(Properties.SSL)!=null&&System.getProperty(Properties.SSL).equalsIgnoreCase("true")){
			proto="https://";
		}

		String port = System.getProperty(Properties.PORT);
		if (port != null) {
			try {
				int portnum = Integer.parseInt(port);
				if (portnum >= 0 && portnum <= 65535) {
					portNumber = portnum;
				}
				else {
					logger.error(String.format(
							"Value specified in option %s (%d) is not valid. Using default value of %d.",
							Properties.PORT, portnum, portNumber));
				}
			} catch (NumberFormatException e) {
				logger.error(String.format(
						"Value specified in option %s (%s) is not a valid numeric value. Using default value of %d.",
						Properties.PORT, port, portNumber));
			}
		} else {
			portNumber =PORT;
		}

	}


}
