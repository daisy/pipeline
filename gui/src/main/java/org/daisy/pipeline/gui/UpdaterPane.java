package org.daisy.pipeline.gui;

import java.io.IOException;

import org.daisy.pipeline.updater.Updater;
import org.daisy.pipeline.updater.UpdaterObserver;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class UpdaterPane extends BorderPane{



        public void build() {
                final VBox controls=new VBox();
                controls.getStyleClass().add("blank");
                final Button butt=new Button("Check for updates");
                final LogPane logPane= new LogPane();
                //logPane.setPadding(new Insets(15, 12, 15, 12));
                final ProgressBar bar= new ProgressBar();
                bar.setVisible(false);
                bar.setProgress(-1);
                bar.setMaxWidth(this.getWidth()-50);
                logPane.getStyleClass().add("messages");
                butt.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                butt.setDisable(true);
                                bar.setVisible(true);
                                new Thread(){
                                        public void run(){
                                                try {
                                                        new Updater().update(logPane);
                                                } catch (IOException e) {
                                                        e.printStackTrace();
                                                        logPane.error("Couldn't start the update process");
                                                } catch (IllegalArgumentException e) {
                                                        e.printStackTrace();
                                                        logPane.error(e.getMessage());
                                                }finally{
                                                        Platform.runLater(new Runnable(){
                                                                public void run(){
                                                                        bar.setVisible(false);
                                                                }
                                                        });
                                                }
                                        }
                                }.start();
                        }
                });
                controls.getChildren().add(butt);

                this.setTop(controls);
                this.setCenter(bar);
                this.setBottom(logPane);

        }
}

class LogPane extends VBox implements UpdaterObserver{
        private ListView<String> messages;
        private ObservableList<String> messageList;

        /**
         *
         */
        public LogPane() {
                super();
                this.getStyleClass().add("messages");
                Text title = new Text("Messages");
                title.getStyleClass().add("subtitle");
                messages = new ListView<String>();
                messages.setCellFactory(new Callback<ListView<String>, 
                                ListCell<String>>() {
                                        @Override 
                                        public ListCell<String> call(ListView<String> list) {
                                                return new ColorRectCell();
                                        }
                }
                );
                this.messageList=FXCollections.observableArrayList();
                messages.setItems(this.messageList);
                this.getChildren().add(title);
                this.getChildren().add(messages);
        }
        public void addMessage(String msg){
                this.messageList.add(msg);
        }
        public void info(final String msg){
                Platform.runLater(new Runnable(){
                        public void run(){
                                messageList.add("INFO "+msg);
                                messages.scrollTo(messageList.size()-1);
                        }
                });

        };
        public void error(final String msg){
                Platform.runLater(new Runnable(){
                        public void run(){
                                messageList.add("ERROR "+msg);
                                messages.scrollTo(messageList.size()-1);
                        }
                });
        };
        static class ColorRectCell extends ListCell<String> {
                @Override
                public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item.startsWith("ERROR ")) {
                                this.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(120,0,0,0.25)"),null,null)));
                        }else{
                                this.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(255,255,255,0.25)"),null,null)));
                        }
                        this.setText(item);
                }
        }

}


