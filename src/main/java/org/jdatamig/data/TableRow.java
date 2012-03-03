package org.jdatamig.data;

import java.util.List;

/**
 *
 * @author Babji, Chetty
 */
public class TableRow {
    private List<TableColumn> columnList;

    /**
     * @return the columnList
     */
    public List<TableColumn> getColumnList() {
        return columnList;
    }

    /**
     * @param columnList the columnList to set
     */
    public void setColumnList(List<TableColumn> columnList) {
        this.columnList = columnList;
    }    
}