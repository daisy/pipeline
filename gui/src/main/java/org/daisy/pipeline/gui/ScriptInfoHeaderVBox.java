package org.daisy.pipeline.gui;

import java.io.IOException;

import org.daisy.pipeline.gui.databridge.Script;
import org.daisy.pipeline.gui.utils.Links;
import org.daisy.pipeline.gui.utils.MarkdownToJavafx;
import org.daisy.pipeline.gui.utils.PlatformUtils;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class ScriptInfoHeaderVBox extends VBox implements MarkdownToJavafx.JavaFxParent{

        MainWindow main;
        MarkdownToJavafx mdToFx ;
        PegDownProcessor mdProcessor ;
        TextFlow flow;
        public ScriptInfoHeaderVBox(MainWindow main) {
                super();
                this.main = main;
                this.getStyleClass().add("script-info");
                this.mdToFx = new MarkdownToJavafx(this);
                this.mdProcessor = new PegDownProcessor();
                
        }
        
        public void populate(Script script) {
                this.flow = new TextFlow();
                Text name = new Text(script.getName());
                name.getStyleClass().add("subtitle");
                this.getChildren().add(name);
                

                RootNode node = mdProcessor.parseMarkdown(script.getDescription().toCharArray());
                //Text desc = new Text(script.getDescription());
                //this.getChildren().add(desc);
                node.accept(this.mdToFx);
                //add description flow
                this.getChildren().add(flow);
                
                
                final String documentationPage = script.getXProcScript().getHomepage();
                
                if (documentationPage != null && documentationPage.isEmpty() == false) { 
                        Hyperlink link = new Hyperlink();
                        link.setText("Read online documentation");
                        link.setAccessibleText("Read online documentation of " + script.getName() + " script");

                        link.setOnAction(Links.getEventHander(main.getHostServices(),documentationPage));
                        this.getChildren().add(link);
                }
        }
        public void clearControls() {
                int sz = getChildren().size();
                if (sz > 0) {
                        getChildren().remove(0, sz); // removes all controls from 0 to sz
                }
        }

        @Override
        public void addChild(Node node) {
                this.flow.getChildren().add(node);
        }
        
        @Override
        public HostServices getHostServices() {
                return main.getHostServices();
        }
}
