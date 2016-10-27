package org.daisy.pipeline.gui;

import java.io.File;
import java.io.IOException;

import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.utils.MarkdownToJavafx;
import org.daisy.pipeline.gui.utils.PlatformUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import javafx.scene.text.TextFlow;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;



// helps with adding common controls to a grid
// keeps track of the row count
public class GridPaneHelper extends GridPane {

        private int rowcount = 0;
        private MainWindow main;
        
        public GridPaneHelper(MainWindow main) {
                super();
                this.main = main;
                this.getStyleClass().add("grid");
        }
        private MarkdownToJavafx.JavaFxParent getJavaFxParent(final Pane parent){
                return new MarkdownToJavafx.JavaFxParent(){
                        @Override
                        public void addChild(Node node) {
                                node.getStyleClass().add("help");
                                parent.getChildren().add(node);

                                //make sure the text is displayed correctly
                                if (node instanceof Text){
                                        Text text = (Text)node;
                                        //if (parent instanceof VBox){
                                                //wrapCorrectly(text, (VBox)parent);
                                        //}else{
                                                //wrapCorrectly(text);
                                                //
                                        //}
                                        text.setWrappingWidth(200);

                                }
                        }

                        @Override
                        public HostServices getHostServices() {
                                return GridPaneHelper.this.main.getHostServices();
                        }

                };
        }
        
        // widths is a series of percent width values
        public void setColumnWidths(int... widths) {            
                for (int width : widths) {
                        ColumnConstraints constraints = new ColumnConstraints();
                        constraints.setPercentWidth(width);
                        this.getColumnConstraints().add(constraints);
                }
        }
        // pass in null values for row spacing
        public void addRow(Node... nodes) {
                int colcount = 0;
                for (Node n : nodes) {
                        if (n == null) {
                                colcount++;
                        }
                        else {
                                n.getStyleClass().add("row");
                                add(n, colcount, rowcount);
                                colcount++;
                        }
                }
                rowcount++;
        }
        
        public void addRow(Node node, int colspan) {
                node.getStyleClass().add("row");
                add(node, 0, rowcount, colspan, 1);
                rowcount++;
        }
        // remove all controls from the grid
        public void clearControls() {
                int sz = getChildren().size();
                
                if (sz > 0) {
                        getChildren().remove(0, sz); // removes all controls from 0 to sz
                }
                rowcount = 0;
        }
        
        // add a link that launches the file or opens the file browser (depending on how the OS interprets the command)
        public void addFinderLinkRow(String label, final String path) {
                Hyperlink link = new Hyperlink();
            link.setText(label);
            link.setTooltip(new Tooltip(path));
        link.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                try {
                        String cmd = PlatformUtils.getFileBrowserCommand() + " " + path;
                                        Runtime.getRuntime().exec(cmd);
                                } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }
            }
        });
        
        addRow(link);
        }
        
        // add two labels in the same row
        public void addNameValuePair(String name, String value) {
                Text nameTxt = new Text(name + ":");
                Text valueTxt = new Text(value);
                addRow(nameTxt, valueTxt);
        }
        
        public void addFileDirPickerSequence(ScriptFieldAnswer.ScriptFieldAnswerList answer) {
                final ListView<String> listbox = new ListView<String>();
                listbox.setItems(answer.answerProperty());
                listbox.getStyleClass().add("input-list");
                Text label = new Text();
                label.setText(answer.getField().getNiceName());
                VBox vbox = new VBox();
                vbox.getChildren().addAll(label);
                addRow(vbox, listbox);
                makeHelpText(answer,vbox);
                //wrapCorrectly(vbox);
                //wrapCorrectly(help, vbox);
                vbox.getStyleClass().add("label-helper-vbox");
                
                
                Button addFileButton = new Button("Add");
                final Button removeFileButton = new Button("Remove");
                HBox hbox = new HBox();
                hbox.getChildren().addAll(addFileButton, removeFileButton);
                hbox.setSpacing(30.0);
                addRow(null, hbox);
                
                final ScriptFieldAnswer.ScriptFieldAnswerList answer_ = answer;
                addFileButton.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                File file;
                                if (answer_.getField().getDataType() == DataType.FILE) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Select File");
                        file = fileChooser.showOpenDialog(null);
                                }
                                // assume directory
                                else {
                                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setTitle("Select Directory");
                        file = dirChooser.showDialog(null);
                                }
                                if(file != null) {
                        answer_.answerProperty().add(file.getPath());
                }
                        }
                });
                
                removeFileButton.setDisable(true);
                removeFileButton.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                if (listbox.getSelectionModel().isEmpty() == false) {
                                        int selection = listbox.getSelectionModel().getSelectedIndex();
                                        listbox.getItems().remove(selection);
                                }
                                
                        }
                });
                listbox.getSelectionModel().selectedItemProperty().addListener(
                    new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> ov, 
                            String old_val, String new_val) {
                                removeFileButton.setDisable(listbox.getSelectionModel().isEmpty());
                    }
                });
                
                
                
                
        }
        public void addTextFieldSequence(ScriptFieldAnswer.ScriptFieldAnswerList answer) {
                Text label = new Text();
                label.setText(answer.getField().getNiceName() + ":");
                final TextField inputText = new TextField();
                Button addTextButton = new Button("Add");
                VBox vbox = new VBox();
                vbox.getChildren().addAll(label);
                addRow(vbox, inputText, addTextButton);
                makeHelpText(answer,vbox);
                //wrapCorrectly(vbox);
                //wrapCorrectly(help, vbox);
                vbox.getStyleClass().add("label-helper-vbox");
                
                final ListView<String> listbox = new ListView<String>();
                listbox.setItems(answer.answerProperty());
                listbox.getStyleClass().add("input-list");
                addRow(null, listbox);
                
                final Button removeTextButton = new Button("Remove");
                addRow(null, removeTextButton);
                
                final ScriptFieldAnswer.ScriptFieldAnswerList answer_ = answer;
                addTextButton.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                if (!inputText.getText().isEmpty()) {
                                        answer_.answerProperty().add(inputText.getText());
                }
                        }
                });
                
                removeTextButton.setDisable(true);
                removeTextButton.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                if (listbox.getSelectionModel().isEmpty() == false) {
                                        int selection = listbox.getSelectionModel().getSelectedIndex();
                                        listbox.getItems().remove(selection);
                                }
                                
                        }
                });
                listbox.getSelectionModel().selectedItemProperty().addListener(
                    new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> ov, 
                            String old_val, String new_val) {
                                removeTextButton.setDisable(listbox.getSelectionModel().isEmpty());
                    }
                });
                
        }
        
        // add a text field with a button for file browsing
        public void addFileDirPicker(ScriptFieldAnswer.ScriptFieldAnswerString answer) {
                Text label = new Text();
                label.setText(answer.getField().getNiceName() + ":");
                final TextField inputFileText = new TextField();
                inputFileText.textProperty().bindBidirectional(answer.answerProperty());
                Button inputFileButton = new Button("Browse");
                VBox vbox = new VBox();
                vbox.getChildren().addAll(label);
                addRow(vbox, inputFileText, inputFileButton);
                makeHelpText(answer,vbox);
                //wrapCorrectly(vbox);
                //wrapCorrectly(help, vbox);
                vbox.getStyleClass().add("label-helper-vbox");
                
                final ScriptFieldAnswer.ScriptFieldAnswerString answer_ = answer;
                inputFileButton.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                File file;
                                if (answer_.getField().getDataType() == DataType.FILE) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Select File");
                        file = fileChooser.showOpenDialog(null);
                                }
                                // assume directory
                                else {
                                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setTitle("Select Directory");
                        file = dirChooser.showDialog(null);
                                }
                                if(file != null) {
                        inputFileText.setText(file.getPath());
                }
                        }
                });
        }
        
        // add the descriptive text to its own row (sometimes it's added in other ways, which is why
        // these help text functions are broken into 3)
        private void addHelpText(ScriptFieldAnswer answer) {
                //makeHelpText(answer,this);
                //wrapCorrectly(text);
                
                
        }
        // create the descriptive text
        private void makeHelpText(ScriptFieldAnswer answer, Pane parent) {

                TextFlow flow = new TextFlow();

                MarkdownToJavafx mdToFx = new MarkdownToJavafx(this.getJavaFxParent(flow));
                PegDownProcessor mdProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
                if (answer.getField().getDescription() != null) {
                	RootNode node = mdProcessor.parseMarkdown(answer.getField().getDescription().toCharArray());
                	node.accept(mdToFx);
                }
                parent.getChildren().add(flow);
                flow.setMaxWidth(350);
                //helpText = helpText.trim();
                //helpText.replace('\n', ' ');
                //helpText.replace('\t', ' ');
                
                //Text help = new Text(answer.getField().getDescription());
                //RootNode node = mdProcessor.parseMarkdown(answer.getField().getDescription().toCharArray());
                ////help.getStyleClass().add("help");
                
                //return help;
        }
        private void wrapCorrectly1(VBox vbox) {
                int col = getColumnIndex(vbox);
                if (col < 0) return;
                vbox.prefWidthProperty().bind(this.getColumnConstraints().get(col).prefWidthProperty());
                
                for (Node node : vbox.getChildren()) {
                        if (node instanceof Text) {
                                ((Text)node).wrappingWidthProperty().bind(vbox.prefWidthProperty());
                        }
                }
        }
        private void wrapCorrectly(Text text) {
                System.out.println("TEXT" + text);
                int col = getColumnIndex(text);
                if (col < 0) return;
                // I'd rather bind the wrappingWidth property but as we just have a percent width
                // on the column, we can't
                text.setWrappingWidth(200); 

                
//              ColumnConstraints constr = this.getColumnConstraints().get(col);
//              System.out.println("$$$$$$$$$$$$$$$$$$$ WIDTHS ");
//              System.out.println("MAX: " + constr.getMaxWidth());
//              System.out.println("MIN: " + constr.getMinWidth());
//              System.out.println("PCT: " + constr.getPercentWidth());
//              System.out.println("PREF: " + constr.getPrefWidth());
        }
        private void wrapCorrectly(Text text, VBox vbox) {
                int col = getColumnIndex(vbox);
                if (col < 0) return;
                text.setWrappingWidth(200);
                
//              ColumnConstraints constr = this.getColumnConstraints().get(col);
//              System.out.println("$$$$$$$$$$$$$$$$$$$ WIDTHS (VBOX) ");
//              System.out.println("MAX: " + constr.getMaxWidth());
//              System.out.println("MIN: " + constr.getMinWidth());
//              System.out.println("PCT: " + constr.getPercentWidth());
//              System.out.println("PREF: " + constr.getPrefWidth());
        }
        // add a checkbox control
        public void addCheckbox(ScriptFieldAnswer.ScriptFieldAnswerBoolean answer) {
                Text label = new Text();
                VBox vbox = new VBox();
                label.setText(answer.getField().getNiceName() + ":");
                vbox.getChildren().add(label);
                CheckBox cb = new CheckBox("");
                if (answer.answerProperty().get() == true) {
                        cb.selectedProperty().set(true);
                }
                cb.selectedProperty().bindBidirectional(answer.answerProperty());
                //vbox.getChildren().add(cb);
                addRow(vbox,cb);
                makeHelpText(answer,vbox);
                
        }
        
        // add a label and a text field
        public void addTextField(ScriptFieldAnswer.ScriptFieldAnswerString answer) {
                Text label = new Text();
                VBox vbox = new VBox();
                label.setText(answer.getField().getNiceName() + ":");
                vbox.getChildren().add(label);
                final TextField textField = new TextField();
                textField.textProperty().bindBidirectional(answer.answerProperty());
                addRow(vbox, textField);
                makeHelpText(answer,vbox);
                
        }

}
