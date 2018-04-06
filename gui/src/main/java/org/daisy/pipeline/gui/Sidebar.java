package org.daisy.pipeline.gui;

import java.util.Comparator;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.daisy.pipeline.gui.databridge.ObservableJob;

public class Sidebar extends VBox {
	
	private final ListView<ObservableJob> jobsList;
	
	public Sidebar(MainWindow main) {
		super();
		this.getStyleClass().add("sidebar");
		jobsList = new ListView<ObservableJob>();
		Label title = new Label("Jobs");
		title.setLabelFor(jobsList);
		title.getStyleClass().add("title");
		this.getChildren().add(title);
		VBox.setVgrow(jobsList, Priority.ALWAYS);
		jobsList.setItems(main.getJobData().sorted(Comparator.reverseOrder()));
		jobsList.setCellFactory(
			list -> new ListCell<ObservableJob>() {
					@Override
					public void updateItem(ObservableJob job, boolean empty) {
						super.updateItem(job, empty);
						if (empty || job == null) {
							textProperty().unbind();
							setText(null);
						} else {
							textProperty().bind(job.toStringProperty());
						}
					}
				}
		);
		jobsList.getSelectionModel().selectedItemProperty().addListener(
			(observable, oldValue, newValue) -> { main.getCurrentJobProperty().set(newValue); }
		);
		this.getChildren().add(jobsList);
		main.getCurrentJobProperty().addListener(
			(observable, oldValue, newValue) -> {
				if (newValue == null) {
					jobsList.getSelectionModel().clearSelection();
				} else {
					jobsList.getSelectionModel().select(newValue);
				}
			}
		);
	}
	
	@Override
	public void requestFocus() {
		jobsList.requestFocus();
	}
}
