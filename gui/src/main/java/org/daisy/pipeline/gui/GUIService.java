package org.daisy.pipeline.gui;

import javafx.application.Application;

import org.daisy.pipeline.properties.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        name = "gui-service",
        immediate = true
)
public class GUIService {

        private static final Logger logger = LoggerFactory.getLogger(GUIService.class);

        private BundleContext ctxt;

        @Activate
        public void start(BundleContext ctxt) {
                if ("gui".equals(Properties.getProperty("org.daisy.pipeline.main.mode"))) {
                        this.ctxt = ctxt;
                        ServiceRegistry.getInstance().setGUIService(this);
                        
                        // The launch method does not return until the application has exited
                        Thread t = new Thread() {
                                public void run() {
                                        Application.launch(PipelineApplication.class);
                                }
                        };
                        t.setPriority(Thread.MAX_PRIORITY);
                        t.start();
                        logger.debug("Main Module is loaded!");
                }
        }

        public void stop() {
                try {
                        ((Framework)this.ctxt.getBundle(0)).stop();
                } catch (BundleException e) {
                        logger.error("Error closing the framework", e);
                        // exit the hard way
                        System.exit(-1);
                }
        }
}
