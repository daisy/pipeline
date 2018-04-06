package org.daisy.pipeline.gui;

import java.util.function.Supplier;

import javafx.application.HostServices;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.daisy.pipeline.gui.databridge.BoundScript;
import org.daisy.pipeline.gui.databridge.DataManager;
import org.daisy.pipeline.gui.databridge.EventBusListener;
import org.daisy.pipeline.gui.databridge.ObservableJob;
import org.daisy.pipeline.gui.databridge.Script;
import org.daisy.pipeline.gui.utils.PlatformUtils;

public class MainWindow extends BorderPane {

	private ServiceRegistry pipelineServices;
    private HostServices hostServices;
    private DataManager dataManager;
    private ObservableList<ObservableJob> jobData;
    private ObservableList<Script> scriptData;
    	
	private Sidebar sidebar;
	private DetailsPane detailsPane;
	private MessagesPane messagesPane;
	private AppMenu menubar;
	private NewJobPane newJobPane;
	private Scene scene;
	private VBox blankPane;
	private ScrollPane scrollPane;
	
	public SimpleObjectProperty<ObservableJob> currentJobProperty;
	private ChangeListener<ObservableJob> currentJobChangeListener;

	static final String APPLICATION_CSS
		= MainWindow.class.getResource("/org/daisy/pipeline/gui/resources/application.css").toExternalForm();
	
	public MainWindow(ServiceRegistry pipelineServices, HostServices hostServices) {
		super();
		this.pipelineServices = pipelineServices;
		this.hostServices = hostServices;
		
		currentJobProperty = new SimpleObjectProperty<ObservableJob>();
		addCurrentJobChangeListener();
		
		jobData = FXCollections.observableArrayList();
		scriptData = FXCollections.observableArrayList();
		dataManager = new DataManager(this, scriptData, pipelineServices);
		pipelineServices.registerEventBusListener(new EventBusListener(pipelineServices, dataManager));
		
		buildWindow();
		
		Supplier<Void> cyclePanes = new Supplier<Void>() {
			int state = 0;
			@Override
			public Void get() {
				switch (state) {
					case 0:
						sidebar.requestFocus();
						state = 1;
						break;
					case 1:
						if (scrollPane.getContent() == detailsPane) {
							detailsPane.requestFocus();
							state = 2;
							break;
						} else if (scrollPane.getContent() == newJobPane) {
							newJobPane.requestFocus();
							state = 2;
							break;
						}
					case 2:
						messagesPane.requestFocus();
						state = 0;
						break;
				}
				return null;
			}
		};
		
		scene.addEventFilter(
			KeyEvent.KEY_PRESSED,
			new EventHandler<KeyEvent>() {
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.F6) {
						cyclePanes.get();
						event.consume();
					}
				}
		});
	}

	public DataManager getDataManager() {
		return dataManager;
	}
	public ObservableList<ObservableJob> getJobData() {
		return jobData;
	}
	public ObservableList<Script> getScriptData() {
		dataManager.readScripts();
		return scriptData;
	}
	public NewJobPane getNewJobPane() {
		return newJobPane;
	}
	public HostServices getHostServices() {
		return hostServices;
	}
	public SimpleObjectProperty<ObservableJob> getCurrentJobProperty() {
		return currentJobProperty;
	}
    private void buildWindow() {
    	scene = new Scene(this ,1024, 768);
		scene.getStylesheets().add(APPLICATION_CSS);
    		
		sidebar = new Sidebar(this);
		//this.setLeft(sidebar);
		
		
		menubar = new AppMenu(this);
		if (PlatformUtils.isMac()) {
			this.getChildren().addAll(menubar);
		}
		else {
			this.setTop(menubar);
		}
		
		scrollPane = new ScrollPane();
		scrollPane.getStyleClass().add("center-scroll");
		
		detailsPane = new DetailsPane(this);
		
		messagesPane = new MessagesPane(this);
		this.setBottom(messagesPane);
		
		blankPane = new VBox();
		blankPane.getChildren().add(new Text("No job selected"));
		blankPane.getStyleClass().add("blank");
		
		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(sidebar, scrollPane);
		splitPane.setDividerPositions(0.3f);
		
		//this.setCenter(scrollPane);
		this.setCenter(splitPane);
		showBlankPane();
		
    }
    
    private void addCurrentJobChangeListener() {
    	final MainWindow thiz = this;
    	currentJobChangeListener = new ChangeListener<ObservableJob>() {

			public void changed(
					ObservableValue<? extends ObservableJob> observable,
					ObservableJob oldValue, ObservableJob newValue) {
				if (newValue == null) {
					thiz.showBlankPane();
					return;
					
				}
				else {
					thiz.showJobDetailsPane();
				}
				
			}
    		
    	};
    	currentJobProperty.addListener(currentJobChangeListener);
    }

    // convenience functions to add/clear validation messages
    public void addValidationMessages(ObservableList<String> messages) {
    	messagesPane.addMessages(messages);
    }
    public void clearValidationMessages() {
    	messagesPane.clearMessages();
    }
    
    private void showNewJobPane() {
    	if (this.scrollPane.getContent() != newJobPane) {
    		scrollPane.setContent(newJobPane);
    	}    	
    }
    private void showJobDetailsPane() {
    	if (scrollPane.getContent() != detailsPane) {
			scrollPane.setContent(detailsPane);
		}
    	menubar.setRunJobEnabled(false);
    }
    private void showBlankPane() {
    	this.scrollPane.setContent(blankPane);
    	menubar.setRunJobEnabled(false);
    }
    
    /* GUI HOOKS (other objects can call these methods to make things happen) */
    public void newJob() {
		currentJobProperty.set(null);
		newJobPane = new NewJobPane(this, pipelineServices);
		newJobPane.getStylesheets().add(APPLICATION_CSS);
		showNewJobPane();
		newJobPane.requestFocus();
	}
    public void enableRunJobMenuItem() {
    	menubar.setRunJobEnabled(true);
    }
	public void runJob() {
		newJobPane.runJob();
	}
    public void deleteSelectedJob() {
    	ObservableJob job = currentJobProperty.get();
    	if (job == null) {
    		return;
    	}
    	
    	pipelineServices.getJobManager().deleteJob(job.getJob().getId());
    	jobData.remove(job);
    	currentJobProperty.set(null);
    }
    
        
    // create a new job based on the currently-selected job
 	// display the new job pane
	public void runSelectedJobAgain() {
		ObservableJob job = currentJobProperty.get();
		// this shouldn't happen... 
		if (job == null) {
			return;
		}
		BoundScript boundScript = dataManager.cloneBoundScript(job.getBoundScript());
		newJobPane.newFromBoundScript(boundScript);
		currentJobProperty.set(null);
		showNewJobPane();
		menubar.setRunJobEnabled(true);
	}
	
	// copy the messages to the clipboard
	public void copyMessages() {
		Iterable<String> messages = messagesPane.getMessages();
		final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        String clipboardString = "";
		
		for (String message : messages) {
			clipboardString += message + "\n";
		}
		content.putString(clipboardString);
		clipboard.setContent(content);
		
	}
}
