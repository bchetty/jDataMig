package org.jdatamig.data;

/**
 *
 * @author Babji, Chetty
 */
public class ReferenceMapping {
    private String referencedTableName;
    private String referencedColumnName;
    private Object referencedColumnValue;

    /**
     * @return the referencedTableName
     */
    public String getReferencedTableName() {
        return referencedTableName;
    }

    /**
     * @param referencedTableName the referencedTableName to set
     */
    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    /**
     * @return the referencedColumnName
     */
    public String getReferencedColumnName() {
        return referencedColumnName;
    }

    /**
     * @param referencedColumnName the referencedColumnName to set
     */
    public void setReferencedColumnName(String referencedColumnName) {
        this.referencedColumnName = referencedColumnName;
    }

    /**
     * @return the referencedColumnValue
     */
    public Object getReferencedColumnValue() {
        return referencedColumnValue;
    }

    /**
     * @param referencedColumnValue the referencedColumnValue to set
     */
    public void setReferencedColumnValue(Object referencedColumnValue) {
        this.referencedColumnValue = referencedColumnValue;
    }
}