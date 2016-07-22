package org.daisy.pipeline.felix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Main that waits for a Runnable service to arrive it can be run in the main thread.
 * The reason for having this launcher is the contraints imposed by MacOS for launching swt application.
 * The main method was just copy-pasted from org.apache.feilx.main 4.4.0
 *
 * The methods waitForSWT just waits for a runnable service and executes it
 */
public class Main extends org.apache.felix.main.Main{
        /**
         * Switch for specifying bundle directory.
         **/
        public static final String BUNDLE_DIR_SWITCH = "-b";
        /**
         * The property name used to specify whether the launcher should
         * install a shutdown hook.
         **/
        public static final String SHUTDOWN_HOOK_PROP = "felix.shutdown.hook";
        /**
         * The property name used to specify an URL to the system
         * property file.
         **/
        public static final String SYSTEM_PROPERTIES_PROP = "felix.system.properties";
        /**
         * The default name used for the system properties file.
         **/
        public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";
        /**
         * The property name used to specify an URL to the configuration
         * property file to be used for the created the framework instance.
         **/
        public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties";
        /**
         * The default name used for the configuration properties file.
         **/
        public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";
        /**
         * Name of the configuration directory.
         */
        public static final String CONFIG_DIRECTORY = "conf";
        private static Framework m_fwk = null;

        public static void main(String[] args) throws Exception {
                // Look for bundle directory and/or cache directory.
                // We support at most one argument, which is the bundle
                // cache directory.
                String bundleDir = null;
                String cacheDir = null;
                boolean expectBundleDir = false;
                for (int i = 0; i < args.length; i++) {
                        if (args[i].equals(BUNDLE_DIR_SWITCH)) {
                                expectBundleDir = true;
                        } else if (expectBundleDir) {
                                bundleDir = args[i];
                                expectBundleDir = false;
                        } else {
                                cacheDir = args[i];
                        }
                }
                if ((args.length > 3) || (expectBundleDir && bundleDir == null)) {
                        System.out
                                        .println("Usage: [-b <bundle-deploy-dir>] [<bundle-cache-dir>]");
                        System.exit(0);
                }
                // Load system properties.
                Main.loadSystemProperties();
                // Read configuration properties.
                Map<String, String> configProps = Main.loadConfigProperties();
                // If no configuration properties were found, then create
                // an empty properties object.
                if (configProps == null) {
                        System.err
                                        .println("No " + CONFIG_PROPERTIES_FILE_VALUE + " found.");
                        configProps = new HashMap<String, String>();
                }
                // Copy framework properties from the system properties.
                Main.copySystemProperties(configProps);
                // If there is a passed in bundle auto-deploy directory, then
                // that overwrites anything in the config file.
                if (bundleDir != null) {
                        configProps.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, bundleDir);
                }
                // If there is a passed in bundle cache directory, then
                // that overwrites anything in the config file.
                if (cacheDir != null) {
                        configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir);
                }
                try {
                        // Create an instance of the framework.
                        FrameworkFactory factory = getFrameworkFactory();
                        m_fwk = factory.newFramework(configProps);
                        // Initialize the framework, but don't start it yet.
                        m_fwk.init();
                        // Use the system bundle context to process the auto-deploy
                        // and auto-install/auto-start properties.
                        AutoProcessor.process(configProps, m_fwk.getBundleContext());
                        FrameworkEvent event;
                        do {
                                // Start the framework.
                                m_fwk.start();
                                waitForSWT(m_fwk);
                                // Wait for framework to stop to exit the VM.
                                event = m_fwk.waitForStop(0);
                        }
                        // If the framework was updated, then restart it.
                        while (event.getType() == FrameworkEvent.STOPPED_UPDATE);
                        // Otherwise, exit.
                        System.exit(0);
                } catch (Exception ex) {
                        System.err.println("Could not create framework: " + ex);
                        ex.printStackTrace();
                        System.exit(0);
                }
        }

        /**
         * Wait for a Runnable service to arrive and execute it
         */
        private static void waitForSWT(Framework fwk) {
                BundleContext bCtxt = fwk.getBundleContext();
                ServiceTracker<Runnable,Runnable> tracker = new ServiceTracker<Runnable,Runnable>(
                                bCtxt, Runnable.class, null);
                Runnable runnable;
                try {
                        tracker.open();
                        runnable = (Runnable)tracker.waitForService(0L);
                        if(runnable!=null){
                                runnable.run();
                        }else{
                                System.err.println("Runnable was null: doing nothing");
                        }
                        tracker.close();
                } catch (Exception e) {
                        System.err.println("Exection in waitForSWT"+e.getMessage());
                        e.printStackTrace();
                }

        }

        /**
         * Simple method to parse META-INF/services file for framework factory.
         * Currently, it assumes the first non-commented line is the class name
         * of the framework factory implementation.
         * @return The created <tt>FrameworkFactory</tt> instance.
         * @throws Exception if any errors occur.
         **/
        private static FrameworkFactory getFrameworkFactory() throws Exception
        {
                URL url = Main.class.getClassLoader().getResource(
                                "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
                if (url != null)
                {
                        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                        try
                        {
                                for (String s = br.readLine(); s != null; s = br.readLine())
                                {
                                        s = s.trim();
                                        // Try to load first non-empty, non-commented line.
                                        if ((s.length() > 0) && (s.charAt(0) != '#'))
                                        {
                                                return (FrameworkFactory) Class.forName(s).newInstance();
                                        }
                                }
                        }
                        finally
                        {
                                if (br != null) br.close();
                        }
                }
                throw new Exception("Could not find framework factory.");
        }
} 

