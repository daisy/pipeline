package org.daisy.pipeline.gui.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pegdown.PegDownProcessor;

import com.google.common.collect.Lists;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
                                                

public class MarkdownToJavafxTest {
        static class MockJavaFxParent implements MarkdownToJavafx.JavaFxParent {
                List<Node> nodes = Lists.newLinkedList();

                @Override
                public void addChild(Node node) {
                        nodes.add(node);

                }
                @Override
                public HostServices getHostServices() {
                        return null;

                }
        }
        //Mock app for testing
        public static class AsNonApp extends Application {
            @Override
            public void start(Stage primaryStage) throws Exception {
                // noop
            }
        }

        MarkdownToJavafx mdToFx ;
        PegDownProcessor mdProcessor ;
        MockJavaFxParent parent = new MockJavaFxParent();


        @BeforeClass
        public static void init() throws Exception{
                // Initialise Java FX

                System.out.printf("About to launch FX App\n");
                Thread t = new Thread("JavaFX Init Thread") {
                        public void run() {
                                Application.launch(AsNonApp.class, new String[0]);
                        }
                };
                t.setDaemon(true);
                t.start();
                System.out.printf("FX App thread started\n");
                Thread.sleep(500);
        }




        @Before
        public void setUp() {
                parent.nodes.clear();
                this.mdToFx = new MarkdownToJavafx(parent);
                this.mdProcessor = new PegDownProcessor();
        }

        @Test
        public void testSimpleText() throws Exception {
                //process hellow world
                mdProcessor.parseMarkdown("Hello world".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The simple text is maintained", ((Text)parent.nodes.get(0)).getText(),"Hello world");
                Assert.assertEquals("And we got the line breaks","\n\n",((Text)parent.nodes.get(1)).getText());
        }

        @Test
        public void testHeaderLevel1() throws Exception {
                //header 1 
                mdProcessor.parseMarkdown("# h1".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"h1");
                Assert.assertEquals("And we got the line break","\n",((Text)parent.nodes.get(1)).getText());
                Assert.assertFalse("The element contains the class h1", ((Text)parent.nodes.get(0)).getStyleClass().indexOf("h1")==-1);

        
        }
        @Test
        public void testHeaderLevel2() throws Exception {

                //header 2 
                mdProcessor.parseMarkdown("## h2".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"h2");
                Assert.assertEquals("And we got the line break","\n",((Text)parent.nodes.get(1)).getText());
                Assert.assertFalse("The element contains the class h2", ((Text)parent.nodes.get(0)).getStyleClass().indexOf("h2")==-1);
        
        }
        @Test
        public void testHeaderLevel6() throws Exception {

                //header 6 
                mdProcessor.parseMarkdown("###### h2".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"h2");
                Assert.assertEquals("And we got the line break","\n",((Text)parent.nodes.get(1)).getText());
                Assert.assertFalse("The element contains the class h6", ((Text)parent.nodes.get(0)).getStyleClass().indexOf("h6")==-1);
        
        }

        @Test
        public void testBoldText() throws Exception {

                mdProcessor.parseMarkdown("**text**".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"text");
                Assert.assertFalse("The element contains the class bold", ((Text)parent.nodes.get(0)).getStyleClass().indexOf(MarkdownToJavafx.STRONG)==-1);
                Assert.assertEquals("And we got the line breaks","\n\n",((Text)parent.nodes.get(1)).getText());
        
        }
        @Test
        public void testEmph() throws Exception {

                mdProcessor.parseMarkdown("*text*".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"text");
                Assert.assertFalse("The element contains the class emph", ((Text)parent.nodes.get(0)).getStyleClass().indexOf(MarkdownToJavafx.EMPH)==-1);
                Assert.assertEquals("And we got the line breaks","\n\n",((Text)parent.nodes.get(1)).getText());
        
        }

@Test
public void testLink() throws Exception {

                mdProcessor.parseMarkdown("[Google](http://www.google.com)".toCharArray()).accept(this.mdToFx);
                Assert.assertEquals("We have two elements, the text and the cr in the parent",2,parent.nodes.size());
                Hyperlink link = (Hyperlink) parent.nodes.get(0);
                Assert.assertEquals("The text has been set",link.getText(),"Google");
                Assert.assertEquals("And we got the line breaks","\n\n",((Text)parent.nodes.get(1)).getText());
        
}

        //@Test
        //public void testStrike() throws Exception {

                //mdProcessor.parseMarkdown("~~strike~~".toCharArray()).accept(this.mdToFx);
                //Assert.assertEquals("We have one element in the parent",1,parent.nodes.size());
                //Assert.assertEquals("The text is maintained", ((Text)parent.nodes.get(0)).getText(),"strike");
                //Assert.assertFalse("The element contains the class bold", ((Text)parent.nodes.get(0)).getStyleClass().indexOf(MarkdownToJavafx.STRIKE)==-1);
        
        //}
}
