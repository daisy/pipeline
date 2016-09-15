package net.kornr.log;

import java.util.Iterator;
import java.util.LinkedList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;

/**
 * The LogBackendActivator registers a LogbackAdaptor object as a LogListener, and 
 * listens to service event, in case some LogReaderService are started or stopped.
 * <p>
 * It registers the BacklogAdaptor to all the LogReaderService available on the 
 * OSGi server.
 * 
 * @author Rodrigo Reyes
 *
 */
public class LogBackendActivator implements BundleActivator, ServiceListener 
{
	private LogbackAdaptor m_backendlogger = new LogbackAdaptor();
	private LinkedList<LogReaderService> m_readers = new LinkedList<LogReaderService>();
	private BundleContext m_context;

	public void start(BundleContext context) throws Exception 
	{
		m_context = context;

		//
		// Register this class as a listener to updates of the service list
		//
		String filter = "(objectclass=" + LogReaderService.class.getName() + ")";
		try {
			context.addServiceListener(this, filter);
		} catch (InvalidSyntaxException e) { 
			e.printStackTrace(); 
		}

		//
		// Register the LogbackAdaptor to all the LogReaderService objects available
		// on the server. That's right, ALL of them.
		//
		ServiceReference[] refs = context.getServiceReferences(org.osgi.service.log.LogReaderService.class.getName(), null);
		if (refs != null)
		{
			for (int i=0; i<refs.length; i++)
			{
				LogReaderService service = (LogReaderService) context.getService(refs[i]);
				if (service != null)
				{
					m_readers.add(service);
					service.addLogListener(m_backendlogger);					
				}
			}
		}
	}

	public void stop(BundleContext context) throws Exception 
	{
		for (Iterator<LogReaderService> i = m_readers.iterator(); i.hasNext(); )
		{
			LogReaderService lrs = i.next();
			lrs.removeLogListener(m_backendlogger);
			i.remove();
		}
	}

	//  We use a ServiceListener to dynamically keep track of all the LogReaderService service being
	//  registered or unregistered
	public void serviceChanged(ServiceEvent event) 
	{
		LogReaderService lrs = (LogReaderService)m_context.getService(event.getServiceReference());
		if (lrs != null)
		{
			if (event.getType() == ServiceEvent.REGISTERED)
			{
				m_readers.add(lrs);
				lrs.addLogListener(m_backendlogger);
			} else if (event.getType() == ServiceEvent.UNREGISTERING)
			{
				lrs.removeLogListener(m_backendlogger);
				m_readers.remove(lrs);
			}
		}
	}

}
