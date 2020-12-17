package org.daisy.pipeline.gui;

import javafx.application.Application;

import org.daisy.common.properties.Properties;
import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
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

        @Activate
        public void start() {
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

        public void stop() {
                if (OSGiHelper.inOSGiContext())
                        OSGiHelper.stopFramework();
                else
                        System.exit(0);
        }

        /**
         * Main method to launch the GUI application.
         */
        public static void main(String[] args) {
                if (args.length > 0) {
                        logger.error("No arguments expected (got '" + String.join(" ", args) + "')");
                        System.exit(1);
                }
                GUIService gui = SPIHelper.createGUIService();
                if (gui == null)
                        System.exit(1);
                System.err.println("Press Ctrl-C to exit");
                // program does not exit until last thread has finished
        }

        // static nested class in order to delay class loading
        private static abstract class OSGiHelper {

                static boolean inOSGiContext() {
                        try {
                                return FrameworkUtil.getBundle(OSGiHelper.class) != null;
                        } catch (NoClassDefFoundError e) {
                                return false;
                        }
                }

                /* Stop Felix */
                static void stopFramework() {
                        try {
                                ((Framework)FrameworkUtil.getBundle(OSGiHelper.class).getBundleContext().getBundle(0)).stop();
                        } catch (BundleException e) {
                                logger.error("Error stopping the framework", e);
                                // exit the hard way
                                System.exit(-1);
                        }
                }
        }

        // static nested class in order to delay class loading
        private static abstract class SPIHelper {

                static GUIService createGUIService() {
                        GUIService gui = null;
                        for (CreateOnStart o : ServiceLoader.load(CreateOnStart.class))
                                if (gui == null && o instanceof GUIService)
                                        gui = (GUIService)o;
                        return gui;
                }
        }
}
