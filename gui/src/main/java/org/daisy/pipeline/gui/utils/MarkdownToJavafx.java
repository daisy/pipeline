package org.daisy.pipeline.gui.utils;
import java.util.LinkedList;
import java.util.List;

import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;

import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

/**
 * Style classes to be applied during the parsing process, 
 * Make sure to make them available when loading the javafx css 
 *
 *  * Headers
 *   - h1 to h6
 *
 *  * Bold text
 *      strong
 *  * Italics
 *      emph
 *  * strike through text
 */



public class MarkdownToJavafx implements Visitor {
        //strong class
        public static final String STRONG = "strong";
        //emph classs
        public static final String EMPH= "emph";
        //strike
        //public static final String STRIKE = "strike";

        public static interface JavaFxParent{
                public void addChild(javafx.scene.Node node);
                public HostServices getHostServices();
        }

        JavaFxParent parent;  
        List<String> styles = new LinkedList<String>();


        /**
         * @param parent
         */
        public MarkdownToJavafx(JavaFxParent parent) {
                this.parent = parent;
                
        }

        @Override
        public void visit(AbbreviationNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(AnchorLinkNode link) {
                

        }

        @Override
        public void visit(AutoLinkNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(BlockQuoteNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(BulletListNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(CodeNode code) {
                Text elem =new Text(code.getText());
                elem.getStyleClass().add("code");
                this.parent.addChild(elem);
        }

        @Override
        public void visit(DefinitionListNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(DefinitionNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(DefinitionTermNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(ExpImageNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(ExpLinkNode node) {
                Hyperlink link = new Hyperlink();
                StringBuffer buff = new StringBuffer();
                printChildren(buff, node);
                link.setText(buff.toString());
                link.setOnAction(Links.getEventHander(this.parent.getHostServices(), node.url));
                parent.addChild(link);

        }


        @Override
    public void visit(HeaderNode header) {
                styles.add("h"+header.getLevel());
                visitChildren(header);
                this.parent.addChild(new Text("\n"));

        }

        @Override
        public void visit(HtmlBlockNode block) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(InlineHtmlNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(ListItemNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(MailLinkNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(OrderedListNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(ParaNode p) {
                visitChildren(p);
                Text newline = new Text("\n");
                this.parent.addChild(newline);

        }

        @Override
        public void visit(QuotedNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(ReferenceNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(RefImageNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(RefLinkNode node) {
                @SuppressWarnings("unused")
                                Hyperlink link = new Hyperlink();


        }

        @Override
        public void visit(RootNode root) {
                visitChildren(root);
        

        }

        @Override
        public void visit(SimpleNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(SpecialTextNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(StrikeNode strike) {
                //TODO the parser doesn't seem to 
                //parse strikes....
                
                //styles.add(STRIKE);
                //visitChildren(strike);

        }

        @Override
        public void visit(StrongEmphSuperNode strong) {
                if (strong.isStrong()){
                        styles.add(STRONG);
                }else{
                        styles.add(EMPH);
                }
                visitChildren(strong);

        }

        @Override
        public void visit(TableBodyNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableCaptionNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableCellNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableColumnNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableHeaderNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(TableRowNode arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public void visit(VerbatimNode verbatim) {
                Text elem =new Text(verbatim.getText());
                elem.getStyleClass().add("code");
                this.parent.addChild(elem);

        }

        @Override
        public void visit(WikiLinkNode arg0) {

        }

        @Override
        public void visit(TextNode text) {
                Text elem =new Text(text.getText());
                for(String style :this.styles){
                        elem.getStyleClass().add(style);
                }

                this.styles.clear();
                this.parent.addChild(elem);

        }

        @Override
        public void visit(SuperNode clarkNode) {
                visitChildren(clarkNode);

        }

        @Override
        public void visit(Node arg0) {
                // TODO Auto-generated method stub

        }

        // helpers
        protected void visitChildren(SuperNode node) {
                for (Node child : node.getChildren()) {
                        child.accept(this);
                }
        } 
        private void printChildren(StringBuffer buff, ExpLinkNode node) {
                //this is ugly as it can get, but again this parser is quite restricted
                //so this is an adhoc method for printing links
                if (node.getChildren().size() > 0 && node.getChildren().get(0).getChildren().size() >0){
                        Node textNode = node.getChildren().get(0).getChildren().get(0);
                        //she sells shells ...
                        if(textNode instanceof TextNode){
                                buff.append(((TextNode)textNode).getText());
                        }

                }
        }
      

        protected String normalize(String string) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < string.length(); i++) {
                        char c = string.charAt(i);
                        switch (c) {
                                case ' ':
                                case '\n':
                                case '\t':
                                        continue;
                        }
                        sb.append(Character.toLowerCase(c));
                }
                return sb.toString();

        }
}
