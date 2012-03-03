package org.jdatamig.importer;

import org.jdatamig.data.TableRow;
import org.jdatamig.data.ExportImportData;
import org.jdatamig.data.Table;
import org.jdatamig.data.ColumnInfo;
import org.jdatamig.data.TableColumn;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author b.chetty
 */
public class XStreamXMLImporter implements XMLImportService {
    private final DataSource dataSource;

    @Inject
    public XStreamXMLImporter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void importFromXML(File xmlFile) {
        FileInputStream fis = null;
        Connection connection = null;

        try {            
            connection = dataSource.getConnection();
            if(connection != null) {
                fis = new FileInputStream(xmlFile);
                final XStream xStream = new XStream(new DomDriver("UTF-8"));
                ExportImportData importData = (ExportImportData) xStream.fromXML(fis);
                if(importData != null) {
                    DatabaseMetaData databaseMetaData = connection.getMetaData();
                                        
                    List<Table> tableDataList = importData.getTableDataList();                    
                    if(tableDataList != null && !tableDataList.isEmpty()) {
                        ResourceBundle bundle = ResourceBundle.getBundle("config");
                        String constraintFields = bundle.getString("constraintFields");
                        List<String> constraintFieldList = null;
                        if(constraintFields != null && !constraintFields.isEmpty()) {
                            constraintFieldList = new ArrayList<String>();
                            constraintFieldList.addAll(Arrays.asList(constraintFields.split(",")));
                        }

                        for(Table table: tableDataList) {
                            System.out.println(table.getTableName());
                            importTableData(connection, databaseMetaData, table, constraintFieldList, null);
                        }
                    }                    
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {            
            try {
                if (fis != null) {                
                    fis.close();                
                }
                if(connection != null) {
                    connection.close();
                }                    
            } catch (SQLException SQLEx) {
                Logger.getLogger(XStreamXMLImporter.class.getName()).log(Level.SEVERE, null, SQLEx);
            } catch (IOException IOEx) {
                Logger.getLogger(XStreamXMLImporter.class.getName()).log(Level.SEVERE, null, IOEx);
            } catch (Exception ex) {
                Logger.getLogger(XStreamXMLImporter.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
    }
    
    /**
     * 
     * @param connection
     * @param databaseMetaData
     * @param table
     * @param constraintFieldList
     * @param primaryKeyName
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Object importTableData(Connection connection, DatabaseMetaData databaseMetaData, Table table, List<String> constraintFieldList, String primaryKeyName) 
            throws SQLException, IOException, ClassNotFoundException {
        String tableName = table.getTableName();
        Object primaryKeyValue = null;
        
        ResultSet resultSet = databaseMetaData.getColumns(null, null, table.getTableName(), null);
        HashMap<String, ColumnInfo> columnInfoMap = null;
        if(resultSet != null) {
            columnInfoMap = new HashMap<String, ColumnInfo>();

            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");  
                String columnType = resultSet.getString("TYPE_NAME");
                boolean nullable = resultSet.getString("NULLABLE").equals("1");
                boolean identity = (columnType != null && columnType.indexOf("identity") >= 0);

                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setIdentity(identity);
                columnInfo.setNullable(nullable);

                columnInfoMap.put(columnName, columnInfo);
            }                                
        }
                        
        List<TableRow> tableRowList = table.getTableRowList();
        if(tableRowList != null && !tableRowList.isEmpty()) {
            for(TableRow tableRow: tableRowList) {
                List<TableColumn> columnList = tableRow.getColumnList();
                if(columnList != null && !columnList.isEmpty()) {
                    Map<String, Object> columnMap = new LinkedHashMap<String, Object>();
                    StringBuilder sbColumnNames = new StringBuilder();
                    StringBuilder sbColumnValues = new StringBuilder();
                        
                    for(TableColumn tableColumn: columnList) {
                        String columnName = tableColumn.getColumnName();
                        Object columnValue = tableColumn.getColumnValue();
                        ColumnInfo columnInfo = columnInfoMap.get(columnName);
                                                
                        if(columnInfo != null && columnInfo.isIdentity()) {
                            continue; //Dont set values for Identity columns
                        }
                        
                        if(columnValue != null) {
                            if(columnValue instanceof Table) {
                                columnMap.put(columnName, importTableData(connection, databaseMetaData, (Table) columnValue, constraintFieldList, tableColumn.getForeignTablePkName()));
                            } else if(tableColumn.isBlobType()) {
                                columnMap.put(columnName, Base64Decode((String) columnValue));
                            } else {
                                columnMap.put(columnName, columnValue);
                            }
                        } else {
                            columnMap.put(columnName, columnValue);                            
                        }
                                                
                        sbColumnNames.append(columnName).append(",");
                        sbColumnValues.append("?,");
                    }
                    
                    String columnNamesCSV = (sbColumnNames.length() > 0) ? sbColumnNames.substring(0, (sbColumnNames.length() -1)) : null;
                    String columnValuesCSV = (sbColumnValues.length() > 0) ? sbColumnValues.substring(0, (sbColumnValues.length() -1)) : null;                    
                    primaryKeyValue = upsert(connection, tableName, columnNamesCSV, columnValuesCSV, columnMap, constraintFieldList, primaryKeyName);
                    
                }
            }
        }
        
        if(resultSet != null) {
            resultSet.close();            
        }        

        return primaryKeyValue;
    }
    
    /**
     * 
     * @param connection
     * @param tableName
     * @param columnNamesCSV
     * @param columnValuesCSV
     * @param columnMap
     * @param constraintFieldList
     * @param primaryKeyName
     * @return
     * @throws SQLException 
     */
    private Object upsert(Connection connection, String tableName, String columnNamesCSV, String columnValuesCSV, Map<String, Object> columnMap, 
            List<String> constraintFieldList, String primaryKeyName) throws SQLException {
        Object primaryKeyValue = null;

        try {
            ResultSet resultSet = null;
            
            if(constraintFieldList != null && !constraintFieldList.isEmpty()) {
                StringBuilder sbCheckDuplicatesQuery = new StringBuilder("select * from " + tableName + " where ");
                boolean constraintPresent = false;
                List<Object> selectedConstraintsList = new ArrayList<Object>();
                for(String constraintField: constraintFieldList) {
                    Object constraintFieldValue = columnMap.get(constraintField.trim());
                    if(constraintFieldValue != null) {                        
                        constraintPresent = true;
                        sbCheckDuplicatesQuery.append(constraintField)
                                              .append(" = ")
                                              .append("?");
                        
                        selectedConstraintsList.add(constraintFieldValue);
                    }
                }

                if(constraintPresent) {
                    System.out.println("Query : " + sbCheckDuplicatesQuery.toString());
                    PreparedStatement prepStmt1 = connection.prepareStatement(sbCheckDuplicatesQuery.toString());
                    
                    int index = 1;
                    for(Object constraintColumnValue: selectedConstraintsList) {
                        findObjectTypeAndSetPrepStmt(prepStmt1, constraintColumnValue, index++);                        
                    }
                    
                    resultSet = prepStmt1.executeQuery();
                }
            }

            //If there is data present already, just link the data to it or else insert.
            if(resultSet != null && resultSet.next()) {
                System.out.println("Row Already Exists!");
                return (primaryKeyName != null && !primaryKeyName.isEmpty()) ? resultSet.getObject(primaryKeyName) : null;
            } else {
                String query = "insert into " + tableName + "(" +  columnNamesCSV + ") values (" + columnValuesCSV + ")";                
                System.out.println("Insert Query : " + query);                
                PreparedStatement prepStmt2 = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                
                Collection<Object> columnValueCol = columnMap.values();                
                if(columnValueCol != null) {
                    int index = 1;
                    for(Object columnValue: columnValueCol) {
                        findObjectTypeAndSetPrepStmt(prepStmt2, columnValue, index++);
                    }
                }
                               
                prepStmt2.executeUpdate();
                resultSet = prepStmt2.getGeneratedKeys();
                if (resultSet.next()) {
                    primaryKeyValue = resultSet.getObject(1);
                }
            }
        } catch(SQLException SQLEx) {
            SQLEx.printStackTrace();
        }

        return primaryKeyValue;
    }
    
    private void findObjectTypeAndSetPrepStmt(PreparedStatement prepStmt, Object columnValue, int index) throws SQLException {
        if(prepStmt != null) {
            if(columnValue != null) {
                if(columnValue instanceof String) {
                    prepStmt.setString(index, (String) columnValue);
                } else if(columnValue instanceof Long) {
                    prepStmt.setLong(index, (Long) columnValue);
                } else if(columnValue instanceof Integer) {
                    prepStmt.setInt(index, (Integer) columnValue);
                } else if(columnValue instanceof Timestamp) {
                    prepStmt.setTimestamp(index, (Timestamp) columnValue);
                } else if(columnValue instanceof Blob) {
                    prepStmt.setBlob(index, (Blob) columnValue);
                }                
            }
            
            prepStmt.setObject(index, columnValue);                        
        }
    }
    
    private Object Base64Decode(String dataString) throws IOException, ClassNotFoundException {        
        return Base64.decodeBase64(dataString.getBytes("UTF-8"));
    }
}
