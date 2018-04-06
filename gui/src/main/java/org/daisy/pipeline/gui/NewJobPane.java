
package org.daisy.pipeline.gui;

import java.text.Collator;
import java.net.MalformedURLException;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.SortedList;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.collections.FXCollections;

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
	private ServiceRegistry pipelineServices;
	private ObservableList<Script> scripts;
	private BoundScript boundScript;
	
	
	private final ComboBox<Script> scriptsCombo = new ComboBox<Script>();
	
	public NewJobPane(MainWindow main, ServiceRegistry pipelineServices) {
		super();
		this.main = main;
		this.pipelineServices = pipelineServices;
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
		main.clearValidationMessages();
	}
	public void newFromBoundScript(BoundScript boundScript) {
		scriptsCombo.getSelectionModel().select(boundScript.getScript());
		
	}
	
	@Override
	public void requestFocus() {
		scriptsCombo.requestFocus();
	}
	
	private void initControls() {
		this.getStyleClass().add("new-job");
	    HBox topGrid = new HBox();
	    this.getChildren().add(topGrid);
	    
	    topGrid.setSpacing(20.0);
	    
		String paneTitle = "New job";
		Label title = new Label("Script:");
		
		// prepend the pane title to the accessible text of the first node
		// so that it is spoken when switching between panes with F6
		title.setAccessibleText(paneTitle + " - Choose a script");
		title.setLabelFor(scriptsCombo);
    //scriptsCombo.accessibleTextProperty().bind( new SimpleStringProperty( ((Script)scriptsCombo.getSelectionModel().selectedItemProperty().getValue()).getName()) );
    //scriptsCombo.accessibleHelpProperty().bind( new SimpleStringProperty( ((Script)scriptsCombo.getSelectionModel().selectedItemProperty().getValue()).getDescription()) );
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
                                // skip "Choose a script" when a script has been selected
                                title.setAccessibleText(paneTitle);
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
                main.clearValidationMessages();
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
                for (ScriptFieldAnswer option : boundScript.getRequiredOptionFields()) {
                        addOptionField(option);
                }
                
                if (Iterators.size(boundScript.getOptionalOptionFields().iterator()) > 0) {
                        Text options = new Text("Options:");
                        options.getStyleClass().add("subtitle");
                        scriptFormControlsGrid.addRow(options);
                }
                for (ScriptFieldAnswer option : boundScript.getOptionalOptionFields()) {
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
                ScriptValidator validator = new ScriptValidator(boundScript);
                if (!validator.validate()) {
                        logger.debug("Script is not valid");
                        ObservableList<String> messages = validator.getMessages();
                        main.addValidationMessages(messages);
                }
                else {
                        Job newJob;
                        try {
                                newJob = JobExecutor.runJob(main, pipelineServices, boundScript);
                                if (newJob != null) {
                                        ObservableJob objob = main.getDataManager().addJob(newJob, boundScript);
                                        main.getCurrentJobProperty().set(objob);
                                } else {
                                        logger.error("Couldn't create the job");
                                }
                        } catch (MalformedURLException e) {
                                ObservableList<String> error= FXCollections.observableArrayList();
                                error.add("Error while trasfroming path: "+e.getMessage());
                                main.addValidationMessages(error);
                                logger.error("Couldn't create the job",e);
                        }
                }
        }
}
