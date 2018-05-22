package org.daisy.pipeline.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.daisy.pipeline.gui.utils.Settings;
import org.daisy.pipeline.gui.utils.Settings.PrefCategories;
import org.daisy.pipeline.gui.utils.Settings.Prefs;
import org.daisy.pipeline.gui.utils.Validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class SettingsPane extends BorderPane {
    
    private Stage parentStage;
    private AnchorPane mainPane;

    private List<Validation<?>> validations;
    
    private Button submitBtn;
    
    private KeyCombination closeCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
    
/*-----SETUP-------------------------------------------------------------*/
    
    public SettingsPane(Stage parentStage) {
        if (parentStage == null)
            throw new NullPointerException("parentStage cannot be null: ok and cancel buttons will not be able to close the stage");
        this.parentStage = parentStage;
        addEventHandler(KeyEvent.KEY_RELEASED, a -> {
            if (closeCombination.match(a))
                parentStage.close();
        });
    }

    public void build() {
        submitBtn = new Button("Ok");
        
        /* CENTER */
        mainPane = new AnchorPane();
        List<FieldSet> categorySets = addCategorySets();
        setupValidations(categorySets);
        setCenter(mainPane);
        
        /* BOTTOM */
        HBox buttonRowBox = new HBox();
        HBox spacerBox = new HBox();
        Button cancelBtn = new Button("Cancel");
        //actions
        submitBtn.setOnAction(a -> submit(categorySets));
        cancelBtn.setOnAction(a -> parentStage.close());
        //LAF
        BorderPane.setMargin(buttonRowBox, new Insets(15));
        buttonRowBox.getStyleClass().add("row");
        HBox.setHgrow(spacerBox, Priority.ALWAYS);
        //default vals
        submitBtn.setDisable(true);
        
        buttonRowBox.getChildren().addAll(submitBtn, spacerBox, cancelBtn);
        setBottom(buttonRowBox);
    }
    
    private void setupValidations(List<FieldSet> categorySets) {
        validations = new ArrayList<Validation<?>>();
        for (FieldSet categorySet: categorySets)
            if (categorySet.getTitle().equals(PrefCategories.JOB_OPTIONS.val())) {
                Label defInDirLbl = categorySet.getLabel(Prefs.DEF_IN_DIR.key()),
                        defOutDirLbl = categorySet.getLabel(Prefs.DEF_OUT_DIR.key());
                TextField defInDirFld = (TextField)categorySet.getNode(TextField.class.getName() + Prefs.DEF_IN_DIR.key()),
                        defOutDirFld = (TextField)categorySet.getNode(TextField.class.getName() + Prefs.DEF_OUT_DIR.key());
                
                validations.add(new Validation<TextField>(defInDirLbl, defInDirFld, fld -> fld.isDisabled() || !isFile(fld.getText()), "Cannot point to a file."));
                validations.add(new Validation<TextField>(defOutDirLbl, defOutDirFld, fld -> fld.isDisabled() || !isFile(fld.getText()), "Cannot point to a file."));
            }
    }
    
    // Helper
    private boolean isFile(String path) {
        return new File(path).isFile();
    }
    
    private void submit(List<FieldSet> categorySets) {
        if (!Validation.run(validations))
            return;
        
        for (FieldSet categorySet: categorySets)
            for (Prefs pref: Prefs.values())
                if (pref.category().val().equals(categorySet.getTitle()))
                    switch (pref.inputType()) {
                        case DIRECTORY_SEQUENCE:
                            TextField fld = (TextField)categorySet.getNode(categorySet.getRow(pref), 2);
                            if (!fld.isDisable())
                                Settings.putString(pref, fld.getText().trim());
                            break;
                        case CHECKBOX:
                            //TODO implement when needed
                            break;
                        default: break;
                    }
        
        parentStage.close();
    }
    
/*---------LAYOUT---------------------------------------------------------------*/
    
    private List<FieldSet> addCategorySets() {
        List<FieldSet> categorySets = new ArrayList<FieldSet>();
        List<PrefCategories> categories = Arrays.asList(PrefCategories.values());
        for (PrefCategories category: categories) {
            FieldSet categorySet = new FieldSet(category.val());
            categorySet.prefWidthProperty().bind(widthProperty());
            layoutDefaults(categorySet, category);
            layoutSpecifics(categorySet, category);
            mainPane.getChildren().add(categorySet);
            categorySets.add(categorySet);
        }
        return categorySets;
    }
    
    private void layoutDefaults(FieldSet categorySet, PrefCategories category) {
        for (Prefs pref: Prefs.values())
            if (pref.category().equals(category))
                switch (pref.inputType()) {
                    case DIRECTORY_SEQUENCE:
                        layoutDirectorySequence(categorySet, pref);
                        break;
                    case CHECKBOX:
                        layoutCheckBox(categorySet, pref);
                        break;
                  default:break;
                }
    }
    
    private void layoutSpecifics(FieldSet categorySet, PrefCategories category) {
        for (Prefs pref: Prefs.values())
            if (pref.category().equals(category)) {
                TextField defInDirFld = (TextField)categorySet.getNode(TextField.class.getName() + Prefs.DEF_IN_DIR.key()),
                        defOutDirFld = (TextField)categorySet.getNode(TextField.class.getName() + Prefs.DEF_OUT_DIR.key());
                switch (pref) {
                    case DEF_IN_DIR:
                        Button copyToOutDirBtn = new Button("▼");
                        copyToOutDirBtn.setOnAction(a -> defOutDirFld.setText(defInDirFld.getText()));
                        copyToOutDirBtn.setTooltip(new Tooltip("Copy contents of default input directory field to default output directory field."));
                        categorySet.addNode(copyToOutDirBtn, categorySet.getRow(defInDirFld));
                        break;
                    case DEF_OUT_DIR:
                        Button copyToInDirBtn = new Button("▲");
                        copyToInDirBtn.setOnAction(a -> defInDirFld.setText(defOutDirFld.getText()));
                        copyToInDirBtn.setTooltip(new Tooltip("Copy contents of default output directory field to default input directory field."));
                        categorySet.addNode(copyToInDirBtn, categorySet.getRow(defOutDirFld));
                        break;
                  default: break;
                }
            }
    }
    
    private void layoutDirectorySequence(FieldSet fieldset, Prefs pref) {
        TextField fld = new TextField();
        Button browseBtn = new Button("Browse");
        
        //default vals
        fld.setText(Settings.getString(pref));
        //actions
        browseBtn.setOnAction(a -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            if (new File(fld.getText()).isDirectory())
                dirChooser.setInitialDirectory(new File(fld.getText()));
            File f = dirChooser.showDialog(parentStage);
            if (f != null)
                fld.setText(f.getAbsolutePath());
        });
        fld.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                submitBtn.setDisable(false);
        });
        
        //LAF
        fieldset.setHGrow(fld, Priority.ALWAYS);
        
        CheckBox enableChkBox = null;
        if (pref.hasEnablePref()) {
            enableChkBox = new CheckBox();
            //default vals
            enableChkBox.setSelected(Settings.getBoolean(pref.enablePref()));
            enableChkBox.setUserData(enableChkBox.getClass().getName());
            fld.setDisable(!Settings.getBoolean(pref.enablePref()));
            browseBtn.setDisable(!Settings.getBoolean(pref.enablePref()));
            //actions
            enableChkBox.setOnAction(a -> {
                CheckBox chkBox = (CheckBox)a.getSource();
                Settings.putBoolean(pref.enablePref(), chkBox.isSelected());
                fieldset.setRowDisabled(fieldset.getRow(chkBox), !chkBox.isSelected(), chkBox);
            });
            //a11y
            enableChkBox.setTooltip(new Tooltip(pref.enablePref().tooltip()));
        }
        
        fld.setUserData(fld.getClass().getName());
        browseBtn.setUserData(browseBtn.getClass().getName());
        setupUserData(pref, enableChkBox, fld, browseBtn);
        //a11y
        fld.setTooltip(new Tooltip(pref.tooltip()));
        browseBtn.setTooltip(new Tooltip("Browse for directory."));
        //add
        fieldset.newRow(pref.key(), pref);
        if (enableChkBox != null)
            fieldset.addNode(enableChkBox);
        fieldset.addNode(fld);
        fieldset.addNode(browseBtn);
        
        fieldset.getLabelFor(pref).setLabelFor(fld);
    }
    
    private void setupUserData(Prefs pref, Node... nodes) {
        for (Node n: nodes)
            if (n != null && n.getUserData() != null)
                n.setUserData(n.getUserData() + pref.key());
            else if (n.getUserData() == null)
                n.setUserData(pref);
    }
    
    private void layoutCheckBox(FieldSet fieldset, Prefs pref) {
        // TODO implement when needed
    }
    
    class CheckBoxToggleListener implements ChangeListener<Boolean> {
        private CheckBox chk;
        
        CheckBoxToggleListener(CheckBox chk) {
            this.chk = chk;
        }
        
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                Boolean newValue) {
            if (newValue)
                chk.setTooltip(new Tooltip(chk.getTooltip().getText().replaceAll("Enable", "Disable")));
            else
                chk.setTooltip(new Tooltip(chk.getTooltip().getText().replaceAll("Disable", "Enable")));
        }
    }
    
}
