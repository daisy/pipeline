package org.daisy.pipeline.job;

import org.daisy.pipeline.event.EventBusProvider;

import com.google.common.eventbus.EventBus;

public class RuntimeConfigurator {

        public interface Monitorable {
                public void setJobMonitor(JobMonitorFactory factory);
        }

        public interface EventBusable {
                public void setEventBus(EventBus bus);
        }

        private EventBus bus;
        private JobMonitorFactory factory;

        public void setEventBus(EventBusProvider provider){
                this.bus=provider.get();
        }

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
