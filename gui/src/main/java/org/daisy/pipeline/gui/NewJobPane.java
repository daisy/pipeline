
package org.daisy.pipeline.gui;

import java.text.Collator;
import java.util.prefs.Preferences;
import java.io.File;
import java.net.MalformedURLException;
import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;


import org.daisy.pipeline.gui.databridge.Script;
import org.daisy.pipeline.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import org.daisy.pipeline.gui.databridge.BoundScript;
import org.daisy.pipeline.gui.databridge.JobExecutor;
import org.daisy.pipeline.gui.databridge.ObservableJob;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.databridge.ScriptValidator;


public class NewJobPane extends VBox {
        private static final Logger logger = LoggerFactory.getLogger(NewJobPane.class);

	
	private ScriptInfoHeaderVBox scriptInfoBox;
	private GridPaneHelper scriptFormControlsGrid;
	private MainWindow main;
	private ObservableList<Script> scripts;
	private BoundScript boundScript;
	
	
	private final ComboBox<Script> scriptsCombo = new ComboBox<Script>();
	
	public NewJobPane(MainWindow main) {
		super();
		this.main = main;
                scripts = main.getScriptData();
                String css = getClass().getResource("/org/daisy/pipeline/gui/resources/application.css").toExternalForm();
                this.getStylesheets().add(css);
                initControls();
                
		
	}
	
	public BoundScript getBoundScript() {
		return boundScript;
	}
	
	// reset the combo selection and clear the script details grid
	public void clearScriptDetails() {
		scriptsCombo.getSelectionModel().clearSelection();
		scriptInfoBox.clearControls();
		scriptFormControlsGrid.clearControls();
		main.getMessagesPane().clearMessages();
	}
	public void newFromBoundScript(BoundScript boundScript) {
		scriptsCombo.getSelectionModel().select(boundScript.getScript());
		
	}
	
	private void initControls() {
		this.getStyleClass().add("new-job");
	    HBox topGrid = new HBox();
	    this.getChildren().add(topGrid);
	    
	    topGrid.setSpacing(20.0);
	    
		Text title = new Text("Choose a script:");
		topGrid.getChildren().add(title);
		
		
		//scriptsCombo.setItems(scripts.sorted());
		scriptsCombo.setItems(new SortedList<Script>(scripts,new Script.ScriptComparator()));
		scriptsCombo.setCellFactory(new Callback<ListView<Script>,ListCell<Script>>(){
			 
            public ListCell<Script> call(ListView<Script> p) {
                final ListCell<Script> cell = new ListCell<Script>(){
                    @Override
                    protected void updateItem(Script t, boolean bln) {
                        super.updateItem(t, bln);
                         
                        if(t != null) {
                            setText(t.getName());
                        }
                        else {
                            setText(null);
                        }
                    }
                };
                return cell;
            }                   
        });
                scriptsCombo.setConverter(new StringConverter<Script>() {
            @Override
            public String toString(Script script) {
              if (script == null){
                return null;
              } 
              else {
                return script.getName();
              }
            }

                        @Override
                        public Script fromString(String string) {
                                // TODO Auto-generated method stub
                                return null;
                        }

          
      });
                
                topGrid.getChildren().add(scriptsCombo);
                
                scriptsCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Script>() {

                        public void changed(ObservableValue<? extends Script> observable,
                                        Script oldValue, Script newValue) {
                                
                                newScriptSelected(newValue);
                        }
                });
                
                scriptInfoBox = new ScriptInfoHeaderVBox(main);
                this.getChildren().add(scriptInfoBox);
                scriptFormControlsGrid = new GridPaneHelper(main);
                scriptFormControlsGrid.setColumnWidths(50, 40, 20);
                this.getChildren().add(scriptFormControlsGrid);
                
        }
        
        private void newScriptSelected(Script script) {
                if (script == null) {
                        return;
                }
                main.getMessagesPane().clearMessages();
                main.enableRunJobMenuItem();
                scriptInfoBox.clearControls();
                scriptFormControlsGrid.clearControls();
                boundScript = new BoundScript(script);
                populateScriptDetailsGrid();
        }
        
        private void populateScriptDetailsGrid() {
                
                scriptInfoBox.populate(boundScript.getScript());
                
                for (ScriptFieldAnswer input : boundScript.getInputFields()) {
                        addInputField(input);
                }
                
                // if this script produces results, add an output directory field. that directory will contain subdirs for each specific output option.
                if (boundScript.getScript().hasResultOptions()) {
                	Preferences prefs = Preferences.userRoot().node("com/org/daisy/pipeline/gui");
                    String lastUsedOutputDir = prefs.get("LastUsedOutputDir", "");
                    boundScript.getOutputDir().set(lastUsedOutputDir);
                    // pass it the property in boundScript to bind the text field widget to
                	scriptFormControlsGrid.addOutputField(boundScript.getOutputDir());
                }
                
                // add the required non-result options
                for (ScriptFieldAnswer option : boundScript.getOptionFields(true)) {
                        addOptionField(option);
                }
                // add the optional non-result options
                if (Iterators.size(boundScript.getOptionFields(false).iterator()) > 0) {
                        Text options = new Text("Options:");
                        options.getStyleClass().add("subtitle");
                        scriptFormControlsGrid.addRow(options);
                }
                for (ScriptFieldAnswer option : boundScript.getOptionFields(false)) {
                        addOptionField(option);
                }
                
                addStandardButtons();
                        
        }
        
        
        private void addInputField(ScriptFieldAnswer answer) {
                if (answer.getField().isSequence()) {
                        scriptFormControlsGrid.addFileDirPickerSequence((ScriptFieldAnswer.ScriptFieldAnswerList)answer);
                }
                else {
                        scriptFormControlsGrid.addFileDirPicker((ScriptFieldAnswer.ScriptFieldAnswerString)answer);
                }
        }

        private void addOptionField(ScriptFieldAnswer answer) {
                DataType fieldDataType = answer.getField().getDataType();
                if (fieldDataType == DataType.FILE || fieldDataType == DataType.DIRECTORY) {
                        if (answer.getField().isSequence()) {
                                scriptFormControlsGrid.addFileDirPickerSequence((ScriptFieldAnswer.ScriptFieldAnswerList)answer);
                        }
                        else {
                                scriptFormControlsGrid.addFileDirPicker((ScriptFieldAnswer.ScriptFieldAnswerString)answer);
                        }
                }
                else if (fieldDataType == DataType.BOOLEAN) {
                        scriptFormControlsGrid.addCheckbox((ScriptFieldAnswer.ScriptFieldAnswerBoolean)answer);
                }
                else {
                        if (answer.getField().isSequence()) {
                                scriptFormControlsGrid.addTextFieldSequence((ScriptFieldAnswer.ScriptFieldAnswerList)answer);
                        }
                        else {
                                scriptFormControlsGrid.addTextField((ScriptFieldAnswer.ScriptFieldAnswerString)answer);
                        }
                        
                }
        }
        
        
        private void addStandardButtons() {
                Button run = new Button("Run");
                run.getStyleClass().add("run-button");
                scriptFormControlsGrid.addRow(run);
                
                run.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent event) {
                                runJob();
                        }
                });
        
        }
        
        public void runJob() {
            main.getMessagesPane().clearMessages();

            ScriptValidator validator = new ScriptValidator(boundScript);
            if (!validator.validate()) {
                    logger.debug("Script is not valid");
                    ObservableList<String> messages = validator.getMessages();
                    main.getMessagesPane().addMessages(messages);
            }
            else {
            		// store the output dir preference, because by this point, we're sure it's valid
            		if (boundScript.getScript().hasResultOptions()) {
            			Preferences prefs = Preferences.userRoot().node("com/org/daisy/pipeline/gui");
            			prefs.put("LastUsedOutputDir", boundScript.getOutputDir().get());
            		}
                    Job newJob;
                    try {
                        newJob = JobExecutor.runJob(main, boundScript);
                        if (newJob != null) {
                                ObservableJob objob = main.getDataManager().addJob(newJob);
                                objob.setBoundScript(boundScript);
                                main.getCurrentJobProperty().set(objob);
                        } else {
                                logger.error("Couldn't create the job");
                        }
                    } catch (MalformedURLException e) {
                        main.getMessagesPane().addMessage("ERROR while transforming path: " + e.getMessage());
                        logger.error("Couldn't create the job",e);
                    }
            }
        }
}
