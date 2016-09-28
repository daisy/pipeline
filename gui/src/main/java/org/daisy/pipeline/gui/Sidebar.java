package org.daisy.pipeline.gui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import org.daisy.pipeline.gui.databridge.ObservableJob;

public class Sidebar extends VBox {
	private TableView<ObservableJob> table;
	private MainWindow main;
	private ChangeListener<ObservableJob> currentJobChangeListener;
	
	public Sidebar(MainWindow main) {
		super();
		this.main = main;
		initControls();
		addCurrentJobChangeListener();
	}
	
	private void initControls() {
		this.getStyleClass().add("sidebar");
		
		table = new TableView<ObservableJob>();
		// this prevents an extra column in the table
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
	    Text title = new Text("Jobs");
	    title.getStyleClass().add("title");
	    this.getChildren().add(title);
	
	    VBox.setVgrow(table, Priority.ALWAYS);
	    
	    table.setItems(main.getJobData());
	    TableColumn<ObservableJob,String> nameCol = new TableColumn<ObservableJob,String>("Name");
	    TableColumn<ObservableJob,String> idCol = new TableColumn<ObservableJob,String>("ID");
	    TableColumn<ObservableJob,String> statusCol = new TableColumn<ObservableJob,String>("Status");
	    
	    nameCol.setCellValueFactory(new Callback<CellDataFeatures<ObservableJob, String>, ObservableValue<String>>() {
	        public ObservableValue<String> call(CellDataFeatures<ObservableJob, String> celldata) {
	            return new ReadOnlyObjectWrapper<String>(celldata.getValue().getBoundScript().getScript().getName());
	        }
	     });
	    
	    idCol.setCellValueFactory(new Callback<CellDataFeatures<ObservableJob, String>, ObservableValue<String>>() {
	        public ObservableValue<String> call(CellDataFeatures<ObservableJob, String> celldata) {
	            return new ReadOnlyObjectWrapper<String>(celldata.getValue().getJob().getId().toString());
	        }
	     });
	    
	    statusCol.setCellValueFactory(new PropertyValueFactory<ObservableJob, String>("status"));
	    
	    table.getColumns().setAll(nameCol, statusCol, idCol);

	    
	    table.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<ObservableJob>() {
                public void changed(ObservableValue<? extends ObservableJob> ov, 
                    ObservableJob old_val, ObservableJob new_val) {
                    main.getCurrentJobProperty().set(new_val);
            }
        });
    
	    
	    this.getChildren().add(table);
	    
	}
	
	private void addCurrentJobChangeListener() {
    	currentJobChangeListener = new ChangeListener<ObservableJob>() {

			public void changed(
					ObservableValue<? extends ObservableJob> observable,
					ObservableJob oldValue, ObservableJob newValue) {
				if (newValue == null) {
					table.getSelectionModel().clearSelection();
				}
				else {
					table.getSelectionModel().select(newValue);
				}
				
			}
    		
    	};
    	main.getCurrentJobProperty().addListener(currentJobChangeListener);
    }
	
}
