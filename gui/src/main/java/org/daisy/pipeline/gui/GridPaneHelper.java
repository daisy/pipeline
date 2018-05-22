package org.daisy.pipeline.gui;

import java.io.File;
import java.io.IOException;

import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptField.FieldType;
import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.utils.MarkdownToJavafx;
import org.daisy.pipeline.gui.utils.PlatformUtils;
import org.daisy.pipeline.gui.utils.Settings;
import org.daisy.pipeline.gui.utils.Settings.Prefs;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import javafx.scene.text.TextFlow;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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

    private MarkdownToJavafx.JavaFxParent getJavaFxParent(final Pane parent) {
        return new MarkdownToJavafx.JavaFxParent() {
            @Override
            public void addChild(Node node) {
                node.getStyleClass().add("help");
                parent.getChildren().add(node);

                //make sure the text is displayed correctly
                if (node instanceof Text) {
                    Text text = (Text)node;
                    //if (parent instanceof VBox) {
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
        Label label = new Label();
        label.setText(answer.getField().getNiceName());
        final ListView<String> listbox = new ListView<String>();
        listbox.setItems(answer.answerProperty());
        listbox.getStyleClass().add("input-list");
        label.setLabelFor(listbox);
        listbox.setTooltip(new Tooltip(answer.getField().getDescription().split("\\r?\\n")[0]));
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
                        setInitialDirPickerDirectory(fileChooser, answer.getField().isResult(),
                                                     answer.getField().isTemp(), answer.getField().getDataType());
                        file = fileChooser.showOpenDialog(null);
                    }
                    // assume directory
                    else {
                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setTitle("Select Directory");
                        setInitialDirPickerDirectory(dirChooser, answer.getField().isResult(),
                                                     answer.getField().isTemp(), answer.getField().getDataType());
                        file = dirChooser.showDialog(null);
                    }
                    if (file != null) {
                        answer_.answerProperty().add(file.getPath());
                        setLastDirectory(file, answer.getField().isResult(),
                                         answer.getField().isTemp(), answer.getField().getDataType());
                    }
                }
            }
        );
                
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
        Label label = new Label();
        label.setText(answer.getField().getNiceName() + ":");
        final TextField inputText = new TextField();
        inputText.setTooltip(new Tooltip(answer.getField().getDescription().split("\\r?\\n")[0]));
        label.setLabelFor(inputText);
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
        listbox.setTooltip(new Tooltip(answer.getField().getDescription().split("\\r?\\n")[0]));
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
        Label label = new Label();
        String labelText = answer.getField().getNiceName() + ":";
        if (answer.getField().isResult()) {
            if (answer.getField().getDataType() == DataType.FILE) {
                labelText = "File for: " + labelText;
            }
            else {
                labelText = "Directory for: " + labelText;
            }
        }
        label.setText(labelText);
        final TextField inputFileText = new TextField();
        inputFileText.textProperty().bindBidirectional(answer.answerProperty());
        label.setLabelFor(inputFileText);
        String desc = answer.getField().getDescription();
        if (desc != null)
            inputFileText.setTooltip(new Tooltip(desc.split("\\r?\\n")[0]));
        setDirFieldDefault(inputFileText, answer.getField().isResult(),
                           answer.getField().isTemp(), answer.getField().getDataType());
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
                        setInitialDirPickerDirectory(fileChooser, answer.getField().isResult(),
                                                     answer.getField().isTemp(), answer.getField().getDataType());
                        file = fileChooser.showOpenDialog(null);
                    }
                    // assume directory
                    else {
                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setTitle("Select Directory");
                        setInitialDirPickerDirectory(dirChooser, answer.getField().isResult(),
                                                     answer.getField().isTemp(), answer.getField().getDataType());
                        file = dirChooser.showDialog(null);
                    }
                    if (file != null) {
                        inputFileText.setText(file.getPath());
                        setLastDirectory(file, answer.getField().isResult(),
                                         answer.getField().isTemp(), answer.getField().getDataType());
                    }
                }
            });
    }

    // a very simple version of the above addFileDirPicker function
    public void addOutputField(SimpleStringProperty outputDir) {
        Text label = new Text("Output directory:");
        final TextField inputFileText = new TextField();
        inputFileText.textProperty().bindBidirectional(outputDir);
        setDirFieldDefault(inputFileText, true, false, DataType.DIRECTORY);
        Button inputFileButton = new Button("Browse");
        VBox vbox = new VBox();
        vbox.getChildren().addAll(label);
        addRow(vbox, inputFileText, inputFileButton);
        vbox.getStyleClass().add("label-helper-vbox");

        inputFileButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select Directory");
                    setInitialDirPickerDirectory(dirChooser, true, false, DataType.DIRECTORY);
                    File file = dirChooser.showDialog(null);
                    if (file != null) {
                        inputFileText.setText(file.getPath());
                        setLastDirectory(file, true, false, DataType.DIRECTORY);
                    }
                }
            });
    }

    private void setDirFieldDefault(TextField field, boolean isResult, boolean isTemp, DataType dataType) {
        String initialDirectory = getInitialDirectory(isResult, isTemp, dataType);
        if (initialDirectory != null)
            field.setText(initialDirectory);
    }

    private void setInitialDirPickerDirectory(FileChooser fileChooser, boolean isResult, boolean isTemp, DataType dataType) {
        String initialDirectory = getInitialDirectory(isResult, isTemp, dataType);
        if (initialDirectory != null)
            fileChooser.setInitialDirectory(new File(initialDirectory));
        // else use system default
    }

    private void setInitialDirPickerDirectory(DirectoryChooser dirChooser, boolean isResult, boolean isTemp, DataType dataType) {
        String initialDirectory = getInitialDirectory(isResult, isTemp, dataType);
        if (initialDirectory != null)
            dirChooser.setInitialDirectory(new File(initialDirectory));
        // else use system default
    }

    private String getInitialDirectory(boolean isResult, boolean isTemp, DataType dataType) {
        String initialDirectory = null;

        //Input
        if (!isResult && !isTemp && dataType == DataType.FILE)
            if (Settings.getBoolean(Prefs.DEF_IN_DIR_ENABLED))
                initialDirectory = Settings.getString(Prefs.DEF_IN_DIR);
            else
                initialDirectory = Settings.getString(Prefs.LAST_IN_DIR);
        // Output
        else if (isResult)
            if (Settings.getBoolean(Prefs.DEF_OUT_DIR_ENABLED))
                initialDirectory = Settings.getString(Prefs.DEF_OUT_DIR);
            else
                initialDirectory = Settings.getString(Prefs.LAST_OUT_DIR);

        return (initialDirectory != null && !initialDirectory.equals(""))? initialDirectory: null;
    }

    private void setLastDirectory(File file, boolean isResult, boolean isTemp, DataType dataType) {
        String fileDirPath = "";

        if (file.isDirectory())
            fileDirPath = file.getPath();
        else // extract parent directory
            fileDirPath = (file.getParent() != null)? file.getParent(): "";

        // Input
        if (!isResult && !isTemp && dataType == DataType.FILE)
            Settings.putString(Prefs.LAST_IN_DIR, fileDirPath);
        // Output
        else if (isResult)
            Settings.putString(Prefs.LAST_OUT_DIR, fileDirPath);
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
        DataType fieldDataType = answer.getField().getDataType();
        if (fieldDataType instanceof DataType.Enumeration) {
            StringBuilder b = new StringBuilder();
            String last = "";
            for (String v : ((DataType.Enumeration)fieldDataType).getValues()) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(last);
                last = v;
            }
            if (b.length() > 0) {
                b.append(" or ");
            }
            b.append(last);
            getJavaFxParent(flow).addChild(new Text("Possible values: " + b + "\n"));
        }
        MarkdownToJavafx mdToFx = new MarkdownToJavafx(this.getJavaFxParent(flow));
        PegDownProcessor mdProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        if (answer.getField().getDescription() != null) {
            RootNode node = mdProcessor.parseMarkdown(answer.getField().getDescription().toCharArray());
            node.accept(mdToFx);
            parent.getChildren().add(flow);
            flow.setMaxWidth(350);
        }
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
        Label label = new Label();
        VBox vbox = new VBox();
        label.setText(answer.getField().getNiceName() + ":");
        vbox.getChildren().add(label);
        CheckBox cb = new CheckBox("");
        if (answer.answerProperty().get() == true) {
            cb.selectedProperty().set(true);
        }
        cb.selectedProperty().bindBidirectional(answer.answerProperty());
        cb.setTooltip(new Tooltip(answer.getField().getDescription().split("\\r?\\n")[0]));
        label.setLabelFor(cb);
        //vbox.getChildren().add(cb);
        addRow(vbox,cb);
        makeHelpText(answer,vbox);
    }

    // add a label and a text field
    public void addTextField(ScriptFieldAnswer.ScriptFieldAnswerString answer) {
        Label label = new Label();
        VBox vbox = new VBox();
        label.setText(answer.getField().getNiceName() + ":");
        vbox.getChildren().add(label);
        final TextField textField = new TextField();
        textField.textProperty().bindBidirectional(answer.answerProperty());
        label.setLabelFor(textField);
        textField.setTooltip(new Tooltip(answer.getField().getDescription().split("\\r?\\n")[0]));
        addRow(vbox, textField);
        makeHelpText(answer,vbox);
    }

}
