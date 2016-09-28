package org.daisy.pipeline.gui.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.HostServices;

public class Links {
        private static final Logger logger = LoggerFactory.getLogger(Links.class);
        public static EventHandler<ActionEvent> getEventHander(HostServices services, String uri){
                return new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                if (!PlatformUtils.isUnix()) {
                                        if (services!= null) {
                                                services.showDocument(uri);
                                        } else {
                                                logger.warn("No services object");
                                        }
                                }else{
                                        String cmd = PlatformUtils.getFileBrowserCommand() + " " + uri;
                                        try {
                                                Runtime.getRuntime().exec(cmd);
                                        } catch (IOException e) {
                                                logger.warn("Error executing browser");
                                        }
                                }
                        }
                };
        }

}
