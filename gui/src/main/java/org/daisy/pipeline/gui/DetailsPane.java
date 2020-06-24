package org.daisy.pipeline.gui;



import java.io.File;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.Text;

import org.daisy.pipeline.gui.databridge.BoundScript;
import org.daisy.pipeline.gui.databridge.ObservableJob;
import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobResult;

public class DetailsPane extends VBox {

	private MainWindow main;
	private ScriptInfoHeaderVBox scriptInfoBox;
	private GridPaneHelper jobInfoGrid;
	private GridPaneHelper resultsGrid; 
	ChangeListener<String> jobStatusListener;
	private ChangeListener<ObservableJob> currentJobChangeListener;
	private final static String paneTitle = "Job details";
	private Labeled firstLabeled;
	private String firstLabeledText;
	
	public DetailsPane(MainWindow main) {
		//super(main);
		super();
		this.main = main;
		scriptInfoBox = new ScriptInfoHeaderVBox(main);
		resultsGrid = new GridPaneHelper(main);
		jobInfoGrid = new GridPaneHelper(main);
		resultsGrid.getStyleClass().add("results");
		addCurrentJobChangeListener();
	}
	
	@Override
	public void requestFocus() {
		Node n = findFirstFocusTraversable(this);
		if (n != null)
			n.requestFocus();
	}
	
	private static Node findFirstFocusTraversable(Parent parent) {
		for (Node n : parent.getChildrenUnmodifiable()) {
			if (n.isFocusTraversable()) {
				return n;
			} else if (n instanceof Parent && (n = findFirstFocusTraversable((Parent)n)) != null) {
				return n;
			}
		}
		return null;
	}
	
	private static Labeled findFirstLabeled(Parent parent) {
		for (Node n : parent.getChildrenUnmodifiable()) {
			if (n instanceof Labeled) {
				return (Labeled)n;
			} else if (n instanceof Parent) {
				Labeled l = findFirstLabeled((Parent)n);
				if (l != null)
					return l;
			}
		}
		return null;
	}
	
	private void displayJobInfo() {
		
		ObservableJob job = this.main.getCurrentJobProperty().get();
		this.getStyleClass().add("details");
		
		Text title = new Text(paneTitle);
		title.getStyleClass().add("title");
		this.getChildren().add(title);
		
		final BoundScript boundScript = job.getBoundScript();

		scriptInfoBox.populate(boundScript.getScript());
		this.getChildren().add(scriptInfoBox);
		
    	this.getChildren().add(jobInfoGrid);
		
		Text statusLabel = new Text("Status:");
		statusLabel.getStyleClass().add("subtitle");
		Text statusValue = new Text();
		statusValue.getStyleClass().add("subtitle");
		
		// binding this causes a thread error
		statusValue.textProperty().bind(job.statusProperty());
		jobInfoGrid.addRow(statusLabel, statusValue);
		
		jobInfoGrid.addNameValuePair("ID", job.getJob().getId().toString());
		
		Text settingsLabel = new Text("Settings:");
		settingsLabel.getStyleClass().add("subtitle");
		jobInfoGrid.addRow(settingsLabel);
		
		for (ScriptFieldAnswer answer : boundScript.getRequiredOptionFields()) {
			addScriptFieldAnswer(answer);
		}
		for (ScriptFieldAnswer answer : boundScript.getOptionalOptionFields()) {
			addScriptFieldAnswer(answer);
		}
		
		this.getChildren().add(resultsGrid);
		
		firstLabeled = findFirstLabeled(this);
		if (firstLabeled != null) {
			firstLabeledText = firstLabeled.getAccessibleText();
			if (firstLabeledText == null) firstLabeledText = firstLabeled.getText();
		}
		
		refreshLinks();
	}
	
	private void refreshLinks() {
		ObservableJob job = this.main.getCurrentJobProperty().get();
		resultsGrid.clearControls();
		
		Status status = job.getJob().getStatus();
		if (status == Status.SUCCESS || status == Status.ERROR || status == Status.FAIL) {
			Text resultsLabel = new Text("Results");
			resultsLabel.getStyleClass().add("subtitle");
	    	resultsGrid.addRow(resultsLabel);
	    	
	    	resultsGrid.addFinderLinkRow("Log file", job.getJob().getContext().getLogFile().toString());
	    	
	    	Iterable<JobResult> results = job.getJob().getContext().getResults().getResults();
	    	for (JobResult result : results) {
	    		File f = new File(result.getPath());
	    		resultsGrid.addFinderLinkRow(f.getName(), result.getPath().toString());
	    	}   	
	    }
		
		// Hack: Prepend the pane title and the job description to the accessible text of the first
		// focusable node so that it is spoken when switching between panes with F6. This node
		// should normally be the "read online documentation" link.
		if (firstLabeled != null)
			firstLabeled.setAccessibleText(paneTitle + " - Job " + job.toString() + " - " + firstLabeledText);
	}
	
	// listen for when the currently selected job changes
	private void addCurrentJobChangeListener() {
    	currentJobChangeListener = new ChangeListener<ObservableJob>() {

			public void changed(
					ObservableValue<? extends ObservableJob> observable,
					ObservableJob oldValue, ObservableJob newValue) {
				setJobStatusListeners();
				clearControls();
				if (newValue != null) {
					displayJobInfo();
				}
			}
    	};
    	main.getCurrentJobProperty().addListener(currentJobChangeListener);
    }
	
	private void setJobStatusListeners() {
		ObservableJob job = this.main.getCurrentJobProperty().get();
		// unhook the old listener
		if (job != null && jobStatusListener != null) {
			job.statusProperty().removeListener(jobStatusListener);
		}
		
		if (job != null) {
			// hook up a new one
			// other job changes (status, messages) are kept up-to-date by having the widget
			// directly observe the relevant property, but in this case we need to observe from the outside
			// and add widgets accordingly
			final DetailsPane thiz = this;
			jobStatusListener = new ChangeListener<String>() {
				public void changed(ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
					
					// need this to avoid "you're on the wrong thread" errors
					Platform.runLater(new Runnable(){
						public void run() {
							thiz.refreshLinks();
						}
					});
				}
			};
			
			job.statusProperty().addListener(jobStatusListener);
		}
	}
	
	private void addScriptFieldAnswer(ScriptFieldAnswer answer) {
		if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerBoolean) {
			ScriptFieldAnswer.ScriptFieldAnswerBoolean answer_ = (ScriptFieldAnswer.ScriptFieldAnswerBoolean)answer;
			jobInfoGrid.addNameValuePair(answer.getField().getNiceName(), answer_.answerAsString());
		}
		else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerString) {
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
			String value = answer_.answerProperty().get();
			if (value.isEmpty()) {
				value = "Not specified";
			}
			jobInfoGrid.addNameValuePair(answer.getField().getNiceName(), value);
		}
		else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerList) {
			ScriptFieldAnswer.ScriptFieldAnswerList answer_ = (ScriptFieldAnswer.ScriptFieldAnswerList)answer;
			int sz = answer_.answerProperty().size();
			if (sz > 0) {
				// add the first one along with the field name
				String value = answer_.answerProperty().get(0);
				if (value.isEmpty()) {
					value = "Not specified";
				}
				jobInfoGrid.addNameValuePair(answer.getField().getNiceName(), value);
				if (sz > 1) {
					// add the rest with blanks in the field column
					for (int i = 1; i<sz; i++) {
						jobInfoGrid.addNameValuePair("", answer_.answerProperty().get(i));
					}
				}
			}
			else {
				// just indicate that there is no value present
				jobInfoGrid.addNameValuePair(answer.getField().getNiceName(), "");
			}
		}
	}
	
	private void clearControls() {
		scriptInfoBox.clearControls();
		jobInfoGrid.clearControls();
		resultsGrid.clearControls();
		
		int sz = getChildren().size();
		if (sz > 0) {
			getChildren().remove(0, sz); // removes all controls from 0 to sz
		}
		
	}
}
