package org.daisy.pipeline.job;

import org.daisy.pipeline.event.EventBusProvider;

import com.google.common.eventbus.EventBus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "runtime-configurator",
    immediate = true,
    service = { RuntimeConfigurator.class }
)
public class RuntimeConfigurator {

        public interface Monitorable {
                public void setJobMonitor(JobMonitorFactory factory);
        }

        public interface EventBusable {
                public void setEventBus(EventBus bus);
        }

        private EventBus bus;
        private JobMonitorFactory factory;

        @Reference(
            name = "event-bus-provider",
            unbind = "-",
            service = EventBusProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
        )
        public void setEventBus(EventBusProvider provider){
                this.bus=provider.get();
        }

        @Reference(
            name = "monitor",
            unbind = "-",
            service = JobMonitorFactory.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
        )
        public void setJobMonitorFactory(JobMonitorFactory factory){
                this.factory=factory;
        }

        public void configure(Job job) {
                if (job instanceof Monitorable && this.factory != null)
                        ((Monitorable)job).setJobMonitor(this.factory);
                if (job instanceof EventBusable && this.bus != null)
                        ((EventBusable)job).setEventBus(this.bus);
        }
}
