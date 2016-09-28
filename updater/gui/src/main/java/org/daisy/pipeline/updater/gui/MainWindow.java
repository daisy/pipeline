package org.daisy.pipeline.updater.gui;

import java.io.IOException;

import org.daisy.pipeline.gui.UpdaterPane;
import org.daisy.pipeline.updater.Updater;
import org.daisy.pipeline.updater.UpdaterObserver;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Callback;
public class MainWindow extends UpdaterPane{
        
       

        public MainWindow(){
                buildWindow();
        }

        private void buildWindow() {

                Scene scene = new Scene(this ,600, 400);
                String css = getClass().getResource("/org/daisy/pipeline/gui/resources/application.css").toExternalForm();
		scene.getStylesheets().add(css);
                super.build();

                
        }

}

