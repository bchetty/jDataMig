package org.jdatamig.exporter;

import org.jdatamig.data.TableRow;
import org.jdatamig.data.ExportImportData;
import org.jdatamig.data.Table;
import org.jdatamig.data.ReferenceMapping;
import org.jdatamig.data.TableColumn;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author b.chetty
 */
public class XStreamXMLExporter implements XMLExportService {
    private final DataSource dataSource;

    @Inject
    public XStreamXMLExporter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 
     * @return
     */
    @Override
    public String exportToXML(List<String> exportTableList) {
        final XStream xStream = new XStream(new DomDriver("UTF-8"));
        String xml = null;
        ExportImportData exportData = null;
        
        try {                
            Connection connection = dataSource.getConnection();
            if(connection != null) {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                String[] types = {"TABLE"};
                ResultSet resultSet1 = databaseMetaData.getTables(null, null, "%", types);
                if(resultSet1 != null) {
                    exportData = new ExportImportData();
                    List<Table> tableDataList = new ArrayList<Table>();
                    int index = 0;
                    
                    while (resultSet1.next()) {
                        String tableCatalog = resultSet1.getString(1);
                        String tableSchema = resultSet1.getString(2);
                        String tableName = resultSet1.getString(3);
                        
                        if(index == 0) {
                            exportData.setDbCatalog(tableCatalog);
                            exportData.setDbSchema(tableSchema);
                        }
                        
                        //System.out.println("Table : " + tableName + "  Table Catalog : " + tableCatalog + "  Table Schema : " + tableSchema);
                        if(exportTableList.contains(tableName)) {
                            tableDataList.add(createTableData(connection, databaseMetaData, tableCatalog, tableSchema, tableName, null));
                        }
                    }
                    
                    exportData.setTableDataList(tableDataList);
                }                
            }

            if (exportData != null) {                
                xml = xStream.toXML(exportData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xml;
    }
    
    /**
     * 
     * @param connection
     * @param databaseMetaData
     * @param tableCatalog
     * @param tableSchema
     * @param tableName
     * @param refTableInfo
     * @return 
     */
    private Table createTableData(Connection connection, DatabaseMetaData databaseMetaData, String tableCatalog, String tableSchema, String tableName,
            ReferenceMapping refTableInfo) {
        ResultSet resultSet = null;
        Table table = null;

        try {
            System.out.println("Table Name : " + tableName + "  Reference Properties -->");
            System.out.println("-----------------------------------------------------------");
            resultSet = databaseMetaData.getImportedKeys(tableCatalog, tableSchema, tableName);
            HashMap<String, ReferenceMapping> referenceMappingMap = new HashMap<String, ReferenceMapping>();
            
            if(resultSet != null) {
                while(resultSet.next()) {
                    String referencedTableName = resultSet.getString("PKTABLE_NAME");
                    String referencedColumnName = resultSet.getString("PKCOLUMN_NAME");
                    String foreignKey = resultSet.getString("FKCOLUMN_NAME");

                    ReferenceMapping refMapping = new ReferenceMapping();
                    refMapping.setReferencedTableName(referencedTableName);
                    refMapping.setReferencedColumnName(referencedColumnName);

                    referenceMappingMap.put(foreignKey, refMapping);
                    System.out.println("Foriegn Key : " + foreignKey + "| Foreign Table : " + referencedTableName + "| Foreign Table Primary Key : " + referencedColumnName);
                }                
            }
            
            System.out.println("-----------------------------------------------------------");

            Statement stmt = connection.createStatement();
            String query = "SELECT * from " + tableName + ((refTableInfo != null) ? " where " + refTableInfo.getReferencedColumnName() + " = " +
                                                                    refTableInfo.getReferencedColumnValue() : "");
            resultSet = stmt.executeQuery(query);
            RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, false, true);
            resultSet.close();
            stmt.close();

            List<DynaBean> rows = rsdc.getRows();
            if(rows != null && !rows.isEmpty()) {
                DynaProperty[] dynaProps = rsdc.getDynaProperties();
                table = new Table();
                table.setTableName(tableName);
                table.setTableRowList(new ArrayList<TableRow>());
                
                for(DynaBean dynaBean: rows) {
                    TableRow tableRow = new TableRow();
                    tableRow.setColumnList(new ArrayList<TableColumn>());

                    for(int i=0;i<dynaProps.length;i++) {                        
                        String columnName = dynaProps[i].getName();
                        Object columnValue = dynaBean.get(dynaProps[i].getName());

                        TableColumn tableColumn = new TableColumn();
                        tableColumn.setColumnName(columnName);
                        
                        if(referenceMappingMap.containsKey(columnName) && columnValue != null) {
                            System.out.println("Forien Key : " + columnName + "  Value : " + columnValue);
                            
                            ReferenceMapping referenceMapping = referenceMappingMap.get(columnName);
                            referenceMapping.setReferencedColumnValue(columnValue);

                            tableColumn.setForeignTablePkName(referenceMapping.getReferencedColumnName());
                            tableColumn.setColumnValue(createTableData(connection, databaseMetaData, tableCatalog, tableSchema, referenceMapping.getReferencedTableName(),
                                    referenceMapping));
                        } else {
                            if(columnValue instanceof Blob) {                                
                                tableColumn.setColumnValue(Base64Encode((Blob) columnValue));
                                tableColumn.setBlobType(true);
                            } else {
                                tableColumn.setColumnValue(columnValue);                                
                            }                            
                        }

                        tableRow.getColumnList().add(tableColumn);
                    }

                    table.getTableRowList().add(tableRow);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return table;
    }
    
    private String Base64Encode(Blob blob) throws IOException, SQLException {        
        return new String(Base64.encodeBase64(blob.getBytes(1, (int) blob.length())));
    }
}