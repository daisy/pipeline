package org.daisy.pipeline.gui;

import java.io.IOException;

import de.codecentric.centerdevice.MenuToolkit;

import org.daisy.pipeline.gui.databridge.ObservableJob;
import org.daisy.pipeline.gui.utils.PlatformUtils;
import org.daisy.pipeline.job.Job.Status;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class AppMenu extends MenuBar {

        private MainWindow main;
        private MenuItem deleteJob;
        private MenuItem runJobAgain;
        private MenuItem runJob;
        private ChangeListener<String> jobStatusListener;
        private ChangeListener<ObservableJob> currentJobChangeListener;

        public AppMenu(MainWindow main) {
                super();
                jobStatusListener = null;
                this.main = main;
                initControls();
                addCurrentJobChangeListener();

        }

        private void initHelp() {
                if (!PlatformUtils.isWin()){
                        Menu menuHelp= new Menu("Help");
                        MenuItem update= new MenuItem("Check updates");
                        MenuItem userguide = new MenuItem("User guide");

                        update.setOnAction(new EventHandler<ActionEvent>() {
                                public void handle(ActionEvent t) {
                                        UpdaterPane updater=new UpdaterPane();
                                        Stage otherStage = new Stage();
                                        Scene secondScene = new Scene(updater, 600, 400);
                                        String css = getClass().getResource("/org/daisy/pipeline/gui/resources/application.css").toExternalForm();

                                        secondScene.getStylesheets().add(css);
                                        updater.build();
                                        otherStage.setScene(secondScene);
                                        otherStage.show();
                                }
                        });
                        userguide.setOnAction(new EventHandler<ActionEvent>() {
                        	public void handle(ActionEvent t) {
                        		try {
                                    String cmd = PlatformUtils.getFileBrowserCommand() + " " + PipelineApplication.USERGUIDE_URL;
                                    Runtime.getRuntime().exec(cmd);
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                	e.printStackTrace();
                                }
                        	}
                        });
                        menuHelp.getItems().add(update);
                        menuHelp.getItems().add(userguide);
                        this.getMenus().addAll(menuHelp);
                }
        }

        private void initControls() {
                Menu menuFile = new Menu("_File");

                this.getMenus().addAll(menuFile);

                MenuItem newjob = new MenuItem("New job");
                newjob.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
                menuFile.getItems().addAll(newjob);
                newjob.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                main.newJob();
                        }
                });

                if (PlatformUtils.isMac()) {
                    this.setUseSystemMenuBar(true);
                    MenuToolkit toolkit = MenuToolkit.toolkit();
                    Menu toolkitMenu = toolkit.createDefaultApplicationMenu("DAISY Pipeline 2");
                    toolkit.setApplicationMenu(toolkitMenu);
                }  else {
                        MenuItem exit = new MenuItem("Exit");
                        exit.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
                        exit.setOnAction(new EventHandler<ActionEvent>() {
                                public void handle(ActionEvent t) {
                                        System.exit(0);
                                }
                        });
                        menuFile.getItems().add(exit);
                }

                runJob = new MenuItem("Run job");
                runJob.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
                runJob.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                main.runJob();
                        }
                });
                menuFile.getItems().add(runJob);
                runJob.setDisable(true);

                deleteJob = new MenuItem("Delete job");
                deleteJob.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
                deleteJob.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                main.deleteSelectedJob();
                        }
                });
                menuFile.getItems().add(deleteJob);
                deleteJob.setDisable(true);

                runJobAgain = new MenuItem("Run job again");
                runJobAgain.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
                runJobAgain.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                main.runSelectedJobAgain();
                        }
                });
                menuFile.getItems().add(runJobAgain);
                runJobAgain.setDisable(true);

                MenuItem copyMessages = new MenuItem("Copy messages to clipboard");
                copyMessages.setOnAction(new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent t) {
                                main.copyMessages();
                        }
                });
                menuFile.getItems().add(copyMessages);

                initHelp();
        }

        public void setRunJobEnabled(boolean value) {
                runJob.setDisable(!value);
        }

        // listen for when the currently selected job changes
        private void addCurrentJobChangeListener() {
                currentJobChangeListener = new ChangeListener<ObservableJob>() {

                        public void changed(
                                        ObservableValue<? extends ObservableJob> observable,
                                        ObservableJob oldValue, ObservableJob newValue) {
                                setJobStatusListeners();
                                        }

                };
                main.getCurrentJobProperty().addListener(currentJobChangeListener);
        }

        // subscribe/unsubscribe to the job's status property
        private void setJobStatusListeners() {
                ObservableJob job = this.main.getCurrentJobProperty().get();
                // unhook the old listener
                if (job != null && jobStatusListener != null) {
                        job.statusProperty().removeListener(jobStatusListener);
                }

                if (job != null) {
                        final AppMenu thiz = this;
                        jobStatusListener = new ChangeListener<String>() {
                                public void changed(ObservableValue<? extends String> observable,
                                                String oldValue, String newValue) {
                                        // somehow when this code is here, the sidebar updates stop working
                                        // even though they aren't linked
                                        Platform.runLater(new Runnable(){
                                                public void run() {
                                                        thiz.enableJobSpecificMenuItems();
                                                }
                                        });

                                }
                        };

                        job.statusProperty().addListener(jobStatusListener);
                }
                enableJobSpecificMenuItems(); // call it now too because we need it to reflect the current status, not just status changes
        }

        // decide whether certain menu items are active
        private void enableJobSpecificMenuItems() {
                ObservableJob job = this.main.getCurrentJobProperty().get();
                if (job == null) {
                        deleteJob.setDisable(true);
                        runJobAgain.setDisable(true);
                }
                else {
                        Status status = job.getJob().getStatus();
                        if (status == Status.SUCCESS || status == Status.ERROR || status == Status.FAIL) {
                                deleteJob.setDisable(false);
                        }
                        runJobAgain.setDisable(false);
                }
        }
}
