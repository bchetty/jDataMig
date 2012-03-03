package org.jdatamig.data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Babji, Chetty
 */
public class Table implements Serializable {
    private String tableName;
    private List<TableRow> tableRowList;
    
    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the tableRowList
     */
    public List<TableRow> getTableRowList() {
        return tableRowList;
    }

    /**
     * @param tableRowList the tableRowList to set
     */
    public void setTableRowList(List<TableRow> tableRowList) {
        this.tableRowList = tableRowList;
    }
}
