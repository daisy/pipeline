package org.daisy.pipeline.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class FieldSet extends GridPane {
    
    private Label titleLbl;
    private int lastRow, lastRealRow; // subtitle and row of controls counts as one row to user
    private List<Integer> cols; // column indexes of last node in each row
    private int totalCols;
    
/*----SETUP--------------------------------------------------------------*/
    
    /**
     * Constructs an empty FieldSet with an empty title.
     */
    public FieldSet() {
        super();
        init(null);
    }
    
    /**
     * Constructs an empty FieldSet with the specified title.
     * 
     * @param title - the title label text
     */
    public FieldSet(String title) {
        super();
        init(title);
    }
    
    private void init(String title) {
        if (title != null)
            addTitle(title);
        getStyleClass().add("fieldset");
        cols = new ArrayList<Integer>();
        cols.add(1); // title col
        cols.add(0);
        lastRow = -1;
        lastRealRow = 1;
        totalCols = 1;
    }
    
/*----PUBLIC--------------------------------------------------------------*/
    
    /**
     * Sets the title label text for this FieldSet (if it wasn't set with the constructor).
     * 
     * @param title - the title label text
     */
    public void setTitle(String title) {
        if (titleLbl != null)
            titleLbl.setText(title);
        else
            addTitle(title);
    }
    
    // Helper
    private void addTitle(String title) {
        getChildren().remove(titleLbl);
        titleLbl = new Label(title);
        titleLbl.getStyleClass().add("subtitle");
        add(titleLbl, 0, 0, 1, 1);
    }
    
    /**
     * Returns the title for this fieldset if it was added.
     *
     * @return the title text String for this FieldSet, empty ("") if not added
     */
    public String getTitle() {
        return (titleLbl != null)? titleLbl.getText(): "";
    }
    
    /**
     * Returns a label in this FieldSet by text.
     * @param text - the text to match
     * @return - the label with text that is {@link Object#equals(Object)} the given text
     */
    public Label getLabel(String text) {
        return findChild(Label.class, child -> child.getText().equals(text));
    }
    
    /**
     * Returns a label for the row with the given rowUserData.
     * @param text - the text to match
     * @return - the label for the row with rowUserData that {@link Object#equals(Object)} the given rowUserData.
     */
    public Label getLabelFor(Object rowUserData) {
        return findChild(Label.class, child -> child.getUserData() != null && child.getUserData().equals(rowUserData));
    }
    
    /**
     * Sets the GridPane Hgrow property for the given node in this FieldSet.
     * 
     * Use this instead of {@link GridPane#setHgrow(Node, Priority)}.
     * 
     * @param node - the node to set the Hgrow property for
     * @param priority -  the horizontal grow priority for the node
     */
    public void setHGrow(Node node, Priority priority) {
        setHgrow(node, priority);
    }
    
    /**
     * Sets the GridPane Hgap property for this FieldSet.
     * 
     * Use this instead of {@link GridPane#setHgap(double)}.
     * 
     * @param hgap - the horizontal gap between all nodes in this FieldSet
     */
    public void setHGap(double hgap) {
        setHgap(hgap);
    }
    
    /**
     * Sets the GridPane Vgap property for this FieldSet.
     * 
     * Use this instead of {@link GridPane#setVgap(double)}.
     * 
     * @param vgap - the vertical gap between all nodes in this FieldSet
     */
    public void setVGap(double vgap) {
        setVgap(vgap);
    }
    
    /**
     * Set the GridPane margin property for the given node.
     * 
     * Use this instead of {@link GridPane#setMargin(Node, Insets)}.
     * 
     * @param node - the node to set the margin for
     * @param margin - the insets to use for the margin
     */
    public void setMarginFor(Node node, Insets margin) {
        GridPane.setMargin(node, margin);
    }
    
    /**
     * Returns the row index for the given node if it was added.
     * 
     * Use this instead of {@link GridPane#getRowIndex(Node)}.
     * 
     * @return null if it wasn't added
     */
    public int getRow(Node node) {
        return convertToRow(getRowIndex(node));
    }
    
    /**
     * Returns the row index for the row with the given rowUserData.
     * 
     * Use this instead of {@link GridPane#getRowIndex(Node)}.
     * 
     * @return null if a row wasn't found
     */
    public int getRow(Object rowUserData) {
        return convertToRow(getRowIndex(findChild(Label.class, lbl -> lbl.getUserData() != null && lbl.getUserData().equals(rowUserData))) + 1);
    }
    
    /**
     * Returns the column index for the given node if it was added.
     * 
     * Use this instead of {@link GridPane#getColumnIndex(Node)}.
     * 
     * @return null if it wasn't added
     */
    public int getColumn(Node node) {
        return getColumnIndex(node);
    }
    
    /**
     * Adds a new empty row to this FieldSet with an empty subtitle.
     */
    public void newRow() {
        newRow(null);
    }
    
    /**
     * Adds a new empty row to this FieldSet with the given subtitle.
     * 
     * @param subtitle - the subtitle label text
     */
    public void newRow(String subtitle) {
        newRow(subtitle, null);
    }
    
    public void newRow(String subtitle, Object rowUserData) {
        Label lbl = null;
        boolean hasSubtitle = false;
        if (subtitle != null && !subtitle.isEmpty()) {
            hasSubtitle = true;
            lbl = new Label(subtitle);
        }
        else
            lbl = new Label("");
        setMarginFor(lbl, new Insets(0, 0, 0, 8));
        add(lbl, cols.get(lastRealRow), lastRealRow, 1, 1);
        lbl.setUserData(rowUserData);
        newRow(hasSubtitle);
    }
    
    // Helper
    private void newRow(boolean hasSubtitle) {
        lastRow++;
        lastRealRow+=2;
        if (hasSubtitle) cols.add(1);
        else cols.add(0);
        cols.add(0);
    }
    
    // Helper - check if totalCols has increased, if so find titles and respan them
    private void respanTitles(int row) {
        if (cols.get(row) > totalCols) {
            totalCols = cols.get(row);
            for (int i = -1; i < lastRealRow; i+=2)
                for (Node child: getChildren())
                    if (getRowIndex(child) == 0 || // title label
                            getRowIndex(child) == i)
                        GridPane.setColumnSpan(child, totalCols);
        }
    }
    
    /**
     * Adds a node to the last row.
     * 
     * Default colSpan and rowSpan is 1.
     * 
     * @param node - the node to add
     */
    public void addNode(Node node) {
        addNode(node, lastRow, 1, 1);
    }
    
    /**
     * Adds a node to the end of the specified row.
     * 
     * Default colSpan and rowSpan is 1.
     * 
     * @param node - the node to add
     * @param row - the row to add the control to
     */
    public void addNode(Node node, int row) {
        addNode(node, row, 1, 1);
    }
    
    /**
     * Adds a node to the end of the specified row with the given colSpan and rowSpan.
     * 
     * @param node -  the node to add
     * @param row - the row to add the node to
     * @param colSpan - determines how many columns the node spans
     * @param rowSpan - determines how many rows the node spans
     */
    public void addNode(Node node, int row, int colSpan, int rowSpan) {
        if (node == null)
            throw new NullPointerException("argument control cannot be null");
        if (row < 0)
            throw new IllegalArgumentException("argument row cannot be negative: " + row);
        if (colSpan <= 0 || rowSpan <= 0)
            throw new IllegalArgumentException("arguments colSpan and rowSpan cannot be less than 1: " + colSpan + ", " + rowSpan);
        if (lastRow == -1) newRow();
        
        int realRow = convertToRealRow(row);
        add(node, cols.get(realRow), realRow, colSpan, rowSpan);
        if (cols.get(realRow) == 1)
            setMargin(node, new Insets(0, 0, 0, 15));
        incrementCols(realRow);
        // make room for rowSpan
        if (rowSpan > 1)
            for (int i = 0; i < rowSpan; i++)
                newRow();
        reorderChildren();
    }
    
    private void incrementCols(int row) {
        cols.set(row, cols.get(row)+1);
        respanTitles(row);
    }
    
    private void reorderChildren() {
        Node[] childrenArray = new Node[getChildren().size()];
        List<Node> children = Arrays.asList(Arrays.copyOf(getChildren().toArray(childrenArray), getChildren().size()));
        
        Collections.sort(children, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (getRowIndex(o1) < getRowIndex(o2))
                    return -1;
                else if (getRowIndex(o1) > getRowIndex(o2))
                    return 1;
                else
                    if (getColumnIndex(o1) < getColumnIndex(o2))
                        return -1;
                    else if (getColumnIndex(o1) > getColumnIndex(o2))
                        return 1;
                    else return 0;
            }
        });
        
        getChildren().clear();
        getChildren().addAll(children);
    }
    
    /**
     * Inserts a node into the specified row and column, shifting all cells in that row to make room.
     * @param node - the node to insert
     * @param row - the row to insert into
     * @param col - the col to insert into
     */
    public void insertNode(Node node, int row, int col) {
        insertNode(node, row, col, 1);
    }
    
    /**
     * Inserts a node into the specified row and column, shifting any cells in that row after the specified position to the right.
     * 
     * Doesn't currently support rowSpan.
     * 
     * @param node -  the node to insert
     * @param row - the row to insert into
     * @param col - the col to insert into
     * @param colSpan - determines how many columns the node spans
     */
    public void insertNode(Node node, int row, int col, int colSpan) {
        if (node == null)
            throw new NullPointerException("argument node cannot be null");
        if (row < 0)
            throw new IllegalArgumentException("argument row cannot be negative: " + row);
        if (colSpan <= 0)
            throw new IllegalArgumentException("argument colSpan cannot be less than 1: " + colSpan);
        
        int realRow = convertToRealRow(row);
        shiftCells(realRow, col, colSpan);
        add(node, col, realRow, colSpan, 1);
    }
    
    /**
     * Returns the node in this FieldSet at the specified row and column.
     * 
     * @return the desired node, null if it does not exist
     */
    public Node getNode(int row, int col) {
        if (row < 0 || col < 0)
            throw new IllegalArgumentException("arguments row and col cannot be negative: " + row + ", " + col);
        return findChild(child -> getRowIndex(child) == convertToRealRow(row) && getColumnIndex(child) == col);
    }
    
    /**
     * Returns the first node in this FieldSet with the specified userData.
     * 
     * @param userData - the userData to search with
     * @return the desired node, null if not found
     */
    public Node getNode(Object userData) {
        return findChild(child -> child.getUserData() != null && child.getUserData().equals(userData));
    }
    
    // Helper
    private void shiftCells(int row, int col, int colSpan) {
        forEachChild(child -> getRowIndex(child) == row && getColumnIndex(child) >= col,
                child -> {
                    GridPane.setColumnIndex(child, col+colSpan);
                    respanTitles(row);
                });
    }
    
    // Helper
    private int convertToRealRow(int row) {
        return row*2+2;
    }
    
    // Helper
    private int convertToRow(int realRow) {
        return (realRow-2)/2;
    }
    
    // Helper
    private <O> O findChild(Class<O> targetClass, Predicate<O> condition) {
        for (Node child: getChildren())
            if (child.getClass().isAssignableFrom(targetClass) && condition.test((O)child))
                return (O)child;
        return null;
    }
    
    // Helper
    private Node findChild(Predicate<Node> condition) {
        for (Node child: getChildren())
            if (condition.test(child))
                return child;
        return null;
    }
    
    // Helper
    private void forEachChild(Predicate<Node> condition, Consumer<Node> consume) {
        for (Node child: getChildren())
            if (condition.test(child))
                consume.accept(child);
    }
    
    /**
     * Disables all nodes in the specified row, excluding the optional exceptions.
     * 
     * Convenience method.
     * 
     * @param row - the row to disable
     * @param disabled - whether to disable the nodes
     * @param exceptions - these nodes in the FieldSet will not be affected (provided they {@link Object#equals()} the given nodes)
     */
    public void setRowDisabled(int row, boolean disabled, Node... exceptions) {
        List<Node> exceptionsList = Arrays.asList(exceptions);
        forEachChild(child -> getRowIndex(child) == convertToRealRow(row) && !exceptionsList.contains(child), 
                child -> child.setDisable(disabled));
    }
    
    /**
     * Disables all nodes in the specified row, excluding the optional exceptions.
     * 
     * Convenience method.
     * 
     * @param row - the row to disable
     * @param disabled - whether to disable the nodes
     * @param userDataExceptions - these nodes in the FieldSet will not be affected (provided their userData {@link Object#equals()} the given userData)
     */
    public void setRowDisabled(int row, boolean disabled, Object... userDataExceptions) {
        List<Object> exceptionsList = Arrays.asList(userDataExceptions);
        forEachChild(child -> getRowIndex(child) == row && !exceptionsList.contains(child.getUserData()),
                child -> child.setDisable(disabled));
    }
    
}
