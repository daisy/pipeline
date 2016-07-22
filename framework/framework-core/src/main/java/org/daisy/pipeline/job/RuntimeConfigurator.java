package org.daisy.pipeline.job;

import org.daisy.pipeline.event.EventBusProvider;

import com.google.common.eventbus.EventBus;

public class RuntimeConfigurator {

        public interface Monitorable{
                public void setJobMonitorFactory(JobMonitorFactory factory);
        }

        public interface EventBusable{
                public void setEventBus(EventBus bus);
        }
      
        private EventBus bus;
        private JobMonitorFactory factory;

        /**
         * @return the bus
         */
        public EventBus getEventBus() {
                return bus;
        }


        /**
         * @return the factory
         */
        public JobMonitorFactory getFactory() {
                return factory;
        }

        public void setEventBus(EventBusProvider provider){
                this.bus=provider.get();
        }

        public void setJobMonitorFactory(JobMonitorFactory factory){
                this.factory=factory;
        }

        public void configure(Monitorable m){
                m.setJobMonitorFactory(this.factory);
        }

        public void configure(EventBusable e){
                e.setEventBus(this.bus);
        }
}
