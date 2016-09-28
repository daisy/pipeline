package org.daisy.pipeline.updater.gui;


import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(final Stage primaryStage) {
            //Optional<String> propsFileName = Optional.fromNullable(System.getProperty("org.daisy.properties",null));
            //if (!propsFileName.isPresent()){
                    //throw new RuntimeException("org.daisy.properties is not set");
            //}

            String pipelineHome =  Advapi32Util.registryGetStringValue(
                                        WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\DAISY Pipeline 2", "Pipeline2Home");
            if (Strings.isNullOrEmpty(pipelineHome)){
                    throw new RuntimeException("Couldn't find the pipeline home in the registry");
            }

            String propsFileName= pipelineHome+"\\etc\\system.properties";


            PropertiesConfiguration config;
            try {
                    config = new PropertiesConfiguration(propsFileName);
                    //for(String prop:System.getProperties().stringPropertyNames()){
                            //config.addProperty(prop,System.getProperties().getProperty(prop));
                    //}
                    config.addProperty("org.daisy.pipeline.home",pipelineHome);
                    config.load();
            } catch (ConfigurationException e) {
                    throw new RuntimeException("Error loading configuration " + e.getMessage(),e);
            }


            //copy the pipeline properties
            for ( Iterator<String> keys = config.getKeys(); keys.hasNext();){
                    String key= keys.next();
                    System.out.println("Setting property "+key+": "+config.getString(key));
                    System.setProperty(key,config.getString(key));
            }
            primaryStage.setTitle("Pipeline 2 updater");
            primaryStage.setScene(new MainWindow().getScene());
            primaryStage.show();
    }
}
