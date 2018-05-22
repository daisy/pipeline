package org.daisy.pipeline.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import org.daisy.pipeline.gui.databridge.ObservableJob;

public class MessagesPane extends VBox {

        private ListView<String> messages;
        private MainWindow main;
        private ChangeListener<ObservableJob> currentJobChangeListener;
        
        public MessagesPane(MainWindow main) {
                super();
                this.main = main;
                initControls();
                addCurrentJobChangeListener();
        }
        
        
        // for job details, the messages come from the pipeline
        // for a new job, the messages pane shows validation messages
        // so, provide a simple way to add and clear messages
        public void addMessages(ObservableList<String> messageList) {
                messages.setItems(messageList);
                Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                                messages.scrollTo(messages.getItems().size()-1);
                                messages.getSelectionModel().select(messages.getItems().size()-1);
                        }
                });
        }
        
        // add a single message
        public void addMessage(String message) {
        	messages.getItems().add(message);
        }
        public void clearMessages() {
                messages.setItems(null);
        }
        public Iterable<String> getMessages() {
                return messages.getItems();
        }
        
        @Override
        public void requestFocus() {
                messages.requestFocus();
        }
        
        private void initControls() {
                this.getStyleClass().add("messages");
                
                Label title = new Label("Messages");
            title.getStyleClass().add("subtitle");
            
            messages = new ListView<String>();
            
                title.setLabelFor(messages);
                this.getChildren().add(title);
                this.getChildren().add(messages);
        
        }
        
        // listen for when the currently selected job changes
                private void addCurrentJobChangeListener() {
                currentJobChangeListener = new ChangeListener<ObservableJob>() {

                                public void changed(
                                                ObservableValue<? extends ObservableJob> observable,
                                                ObservableJob oldValue, ObservableJob newValue) {
                                        if (newValue == null) {
                                                messages.setItems(null);
                                        }
                                        else {
                                                newValue.getMessages().addListener(
                                                                new ListChangeListener<String>() {

                                                                        @Override
                                                                        public void onChanged(
                                                                                javafx.collections.ListChangeListener.Change<? extends String> arg0) {
                                                                                Platform.runLater(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                                messages.scrollTo(messages.getItems().size()-1);
                                                                                        }
                                                                                });

                                                                                }
                                                                });
                                                messages.setItems(newValue.getMessages());
                                        }

                                }
                        
                };
                main.getCurrentJobProperty().addListener(currentJobChangeListener);
            }

        
}
