package org.jdatamig.data;

/**
 *
 * @author Babji, Chetty
 */
public class TableColumn {    
    private String columnName;    
    private Object columnValue;
    private String foreignTablePkName;
    private boolean blobType;
    
    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * @return the columnValue
     */
    public Object getColumnValue() {
        return columnValue;
    }

    /**
     * @param columnValue the columnValue to set
     */
    public void setColumnValue(Object columnValue) {
        this.columnValue = columnValue;
    }

    /**
     * @return the foreignTablePkName
     */
    public String getForeignTablePkName() {
        return foreignTablePkName;
    }

    /**
     * @param foreignTablePkName the foreignTablePkName to set
     */
    public void setForeignTablePkName(String foreignTablePkName) {
        this.foreignTablePkName = foreignTablePkName;
    }

    /**
     * @return the blobType
     */
    public boolean isBlobType() {
        return blobType;
    }

    /**
     * @param blobType the blobType to set
     */
    public void setBlobType(boolean blobType) {
        this.blobType = blobType;
    }
}