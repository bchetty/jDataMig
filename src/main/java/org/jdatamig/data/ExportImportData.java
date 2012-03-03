package org.jdatamig.data;

import java.util.List;

/**
 *
 * @author Babji, Chetty
 */
public class ExportImportData {
    private String dbSchema;
    private String dbCatalog;
    private List<Table> tableDataList;

    /**
     * @return the dbSchema
     */
    public String getDbSchema() {
        return dbSchema;
    }

    /**
     * @param dbSchema the dbSchema to set
     */
    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    /**
     * @return the dbCatalog
     */
    public String getDbCatalog() {
        return dbCatalog;
    }

    /**
     * @param dbCatalog the dbCatalog to set
     */
    public void setDbCatalog(String dbCatalog) {
        this.dbCatalog = dbCatalog;
    }

    /**
     * @return the tableDataList
     */
    public List<Table> getTableDataList() {
        return tableDataList;
    }

    /**
     * @param tableDataList the tableDataList to set
     */
    public void setTableDataList(List<Table> tableDataList) {
        this.tableDataList = tableDataList;
    }
}
