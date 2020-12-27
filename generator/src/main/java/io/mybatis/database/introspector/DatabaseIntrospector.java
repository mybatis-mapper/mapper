/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.database.introspector;

import io.mybatis.database.DatabaseConfig;
import io.mybatis.database.FullyQualifiedJavaType;
import io.mybatis.database.IntrospectedColumn;
import io.mybatis.database.IntrospectedTable;
import io.mybatis.database.utils.DBMetadataUtils;
import io.mybatis.database.utils.JavaBeansUtil;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class DatabaseIntrospector {

  protected static final Map<Integer, JdbcTypeInformation> typeMap;

  static {
    typeMap = new HashMap<Integer, JdbcTypeInformation>();

    typeMap.put(Types.ARRAY, new JdbcTypeInformation("ARRAY",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.BIGINT, new JdbcTypeInformation("BIGINT",
        new FullyQualifiedJavaType(Long.class.getName())));
    typeMap.put(Types.BINARY, new JdbcTypeInformation("BINARY",
        new FullyQualifiedJavaType("byte[]")));
    typeMap.put(Types.BIT, new JdbcTypeInformation("BIT",
        new FullyQualifiedJavaType(Boolean.class.getName())));
    typeMap.put(Types.BLOB, new JdbcTypeInformation("BLOB",
        new FullyQualifiedJavaType("byte[]")));
    typeMap.put(Types.BOOLEAN, new JdbcTypeInformation("BOOLEAN",
        new FullyQualifiedJavaType(Boolean.class.getName())));
    typeMap.put(Types.CHAR, new JdbcTypeInformation("CHAR",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.CLOB, new JdbcTypeInformation("CLOB",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.DATALINK, new JdbcTypeInformation("DATALINK",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.DATE, new JdbcTypeInformation("DATE",
        new FullyQualifiedJavaType(Date.class.getName())));
    typeMap.put(Types.DISTINCT, new JdbcTypeInformation("DISTINCT",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.DOUBLE, new JdbcTypeInformation("DOUBLE",
        new FullyQualifiedJavaType(Double.class.getName())));
    typeMap.put(Types.FLOAT, new JdbcTypeInformation("FLOAT",
        new FullyQualifiedJavaType(Double.class.getName())));
    typeMap.put(Types.INTEGER, new JdbcTypeInformation("INTEGER",
        new FullyQualifiedJavaType(Integer.class.getName())));
    typeMap.put(Types.JAVA_OBJECT, new JdbcTypeInformation("JAVA_OBJECT",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.LONGNVARCHAR, new JdbcTypeInformation("LONGNVARCHAR",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.LONGVARBINARY, new JdbcTypeInformation(
        "LONGVARBINARY",
        new FullyQualifiedJavaType("byte[]")));
    typeMap.put(Types.LONGVARCHAR, new JdbcTypeInformation("LONGVARCHAR",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.NCHAR, new JdbcTypeInformation("NCHAR",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.NCLOB, new JdbcTypeInformation("NCLOB",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.NVARCHAR, new JdbcTypeInformation("NVARCHAR",
        new FullyQualifiedJavaType(String.class.getName())));
    typeMap.put(Types.NULL, new JdbcTypeInformation("NULL",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.OTHER, new JdbcTypeInformation("OTHER",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.REAL, new JdbcTypeInformation("REAL",
        new FullyQualifiedJavaType(Float.class.getName())));
    typeMap.put(Types.REF, new JdbcTypeInformation("REF",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.SMALLINT, new JdbcTypeInformation("SMALLINT",
        new FullyQualifiedJavaType(Short.class.getName())));
    typeMap.put(Types.STRUCT, new JdbcTypeInformation("STRUCT",
        new FullyQualifiedJavaType(Object.class.getName())));
    typeMap.put(Types.TIME, new JdbcTypeInformation("TIME",
        new FullyQualifiedJavaType(Date.class.getName())));
    typeMap.put(Types.TIMESTAMP, new JdbcTypeInformation("TIMESTAMP",
        new FullyQualifiedJavaType(Date.class.getName())));
    typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT",
        new FullyQualifiedJavaType(Byte.class.getName())));
    typeMap.put(Types.VARBINARY, new JdbcTypeInformation("VARBINARY",
        new FullyQualifiedJavaType("byte[]")));
    typeMap.put(Types.VARCHAR, new JdbcTypeInformation("VARCHAR",
        new FullyQualifiedJavaType(String.class.getName())));
  }

  protected DBMetadataUtils dbMetadataUtils;
  protected boolean         forceBigDecimals;
  protected boolean         useCamelCase;

  public DatabaseIntrospector(DBMetadataUtils dbMetadataUtils) {
    this(dbMetadataUtils, false, true);
  }

  public DatabaseIntrospector(DBMetadataUtils dbMetadataUtils, boolean forceBigDecimals, boolean useCamelCase) {
    this.dbMetadataUtils = dbMetadataUtils;
    this.forceBigDecimals = forceBigDecimals;
    this.useCamelCase = useCamelCase;
  }

  public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {
    FullyQualifiedJavaType answer;
    JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());
    if (jdbcTypeInformation == null) {
      switch (introspectedColumn.getJdbcType()) {
        case Types.DECIMAL:
        case Types.NUMERIC:
          if (introspectedColumn.getScale() > 0
              || introspectedColumn.getLength() > 18
              || forceBigDecimals
          ) {
            answer = new FullyQualifiedJavaType(BigDecimal.class
                .getName());
          } else if (introspectedColumn.getLength() > 9) {
            answer = new FullyQualifiedJavaType(Long.class.getName());
          } else if (introspectedColumn.getLength() > 4) {
            answer = new FullyQualifiedJavaType(Integer.class.getName());
          } else {
            answer = new FullyQualifiedJavaType(Short.class.getName());
          }
          break;

        default:
          answer = null;
          break;
      }
    } else {
      answer = jdbcTypeInformation.getFullyQualifiedJavaType();
    }

    return answer;
  }

  public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
    String answer;
    JdbcTypeInformation jdbcTypeInformation = typeMap
        .get(introspectedColumn.getJdbcType());

    if (jdbcTypeInformation == null) {
      switch (introspectedColumn.getJdbcType()) {
        case Types.DECIMAL:
          answer = "DECIMAL";
          break;
        case Types.NUMERIC:
          answer = "NUMERIC";
          break;
        default:
          answer = null;
          break;
      }
    } else {
      answer = jdbcTypeInformation.getJdbcTypeName();
    }

    return answer;
  }

  public List<String> getCatalogs() throws SQLException {
    ResultSet rs = dbMetadataUtils.getDatabaseMetaData().getCatalogs();
    List<String> catalogs = new ArrayList<String>();
    while (rs.next()) {
      catalogs.add(rs.getString(1));
    }
    closeResultSet(rs);
    return catalogs;
  }

  public List<String> getSchemas() throws SQLException {
    ResultSet rs = dbMetadataUtils.getDatabaseMetaData().getSchemas();
    List<String> schemas = new ArrayList<String>();
    while (rs.next()) {
      schemas.add(rs.getString(1));
    }
    closeResultSet(rs);
    return schemas;
  }

  public List<String> getTableTypes() throws SQLException {
    ResultSet rs = dbMetadataUtils.getDatabaseMetaData().getTableTypes();
    List<String> tableType = new ArrayList<String>();
    while (rs.next()) {
      tableType.add(rs.getString(1));
    }
    closeResultSet(rs);
    return tableType;
  }

  /**
   * 计算主键
   *
   * @param config
   * @param introspectedTable
   */
  protected void calculatePrimaryKey(DatabaseConfig config,
                                     IntrospectedTable introspectedTable) {
    ResultSet rs = null;
    try {
      rs = dbMetadataUtils.getDatabaseMetaData().getPrimaryKeys(
          config.getCatalog(),
          config.getSchemaPattern(),
          introspectedTable.getName());
    } catch (SQLException e) {
      closeResultSet(rs);
      return;
    }

    try {
      Map<Short, String> keyColumns = new TreeMap<Short, String>();
      while (rs.next()) {
        //主键列名
        String columnName = rs.getString("COLUMN_NAME");
        //主键顺序
        short keySeq = rs.getShort("KEY_SEQ");
        keyColumns.put(keySeq, columnName);
      }

      for (String columnName : keyColumns.values()) {
        introspectedTable.addPrimaryKeyColumn(columnName);
      }
    } catch (SQLException e) {
    } finally {
      closeResultSet(rs);
    }
  }

  /**
   * 关闭ResultSet
   *
   * @param rs
   */
  protected void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
      }
    }
  }

  /**
   * 获取表信息
   *
   * @param config
   * @return
   * @throws SQLException
   */
  public List<IntrospectedTable> introspectTables(DatabaseConfig config)
      throws SQLException {
    if (config.hasProcess()) {
      config.getDatabaseProcess().processStart();
    }
    List<IntrospectedTable> introspectedTables = null;
    try {
      DatabaseConfig localConfig = getLocalDatabaseConfig(config);
      Map<IntrospectedTable, List<IntrospectedColumn>> columns = getColumns(localConfig);

      if (columns.isEmpty()) {
        introspectedTables = new ArrayList<IntrospectedTable>(0);
      } else {
        introspectedTables = calculateIntrospectedTables(localConfig, columns);
        Iterator<IntrospectedTable> iter = introspectedTables.iterator();
        while (iter.hasNext()) {
          IntrospectedTable introspectedTable = iter.next();
          //去掉没有字段的表
          if (!introspectedTable.hasAnyColumns()) {
            iter.remove();
          }
        }
      }
    } finally {
      if (config.hasProcess()) {
        config.getDatabaseProcess().processComplete(introspectedTables);
      }
    }
    return introspectedTables;
  }

  /**
   * 根据数据库转换配置
   *
   * @param config
   * @return
   * @throws SQLException
   */
  protected DatabaseConfig getLocalDatabaseConfig(DatabaseConfig config) throws SQLException {
    String localCatalog;
    String localSchema;
    String localTableName;
    if (dbMetadataUtils.getDatabaseMetaData().storesLowerCaseIdentifiers()) {
      localCatalog = config.getCatalog() == null ? null : config.getCatalog()
          .toLowerCase();
      localSchema = config.getSchemaPattern() == null ? null : config.getSchemaPattern()
          .toLowerCase();
      localTableName = config.getTableNamePattern() == null ? null : config
          .getTableNamePattern().toLowerCase();
    } else if (dbMetadataUtils.getDatabaseMetaData().storesUpperCaseIdentifiers()) {
      localCatalog = config.getCatalog() == null ? null : config.getCatalog()
          .toUpperCase();
      localSchema = config.getSchemaPattern() == null ? null : config.getSchemaPattern()
          .toUpperCase();
      localTableName = config.getTableNamePattern() == null ? null : config
          .getTableNamePattern().toUpperCase();
    } else {
      localCatalog = config.getCatalog();
      localSchema = config.getSchemaPattern();
      localTableName = config.getTableNamePattern();
    }
    DatabaseConfig newConfig = new DatabaseConfig(localCatalog, localSchema, localTableName);
    newConfig.setDatabaseProcess(config.getDatabaseProcess());
    return newConfig;
  }

  /**
   * 获取全部的表和字段
   *
   * @param config
   * @return
   * @throws SQLException
   */
  protected Map<IntrospectedTable, List<IntrospectedColumn>> getColumns(DatabaseConfig config) throws SQLException {
    Map<IntrospectedTable, List<IntrospectedColumn>> answer = new HashMap<IntrospectedTable, List<IntrospectedColumn>>();

    ResultSet rs = dbMetadataUtils.getDatabaseMetaData().getColumns(
        config.getCatalog(),
        config.getSchemaPattern(),
        config.getTableNamePattern(),
        null);
    while (rs.next()) {
      IntrospectedColumn column = new IntrospectedColumn();
      column.setJdbcType(rs.getInt("DATA_TYPE"));
      column.setType(rs.getString("TYPE_NAME"));
      column.setLength(rs.getInt("COLUMN_SIZE"));
      column.setName(rs.getString("COLUMN_NAME"));
      column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
      column.setScale(rs.getInt("DECIMAL_DIGITS"));
      column.setRemarks(rs.getString("REMARKS"));
      column.setDefaultValue(rs.getString("COLUMN_DEF"));

      IntrospectedTable table = new IntrospectedTable(
          rs.getString("TABLE_CAT"),
          rs.getString("TABLE_SCHEM"),
          rs.getString("TABLE_NAME"));

      List<IntrospectedColumn> columns = answer.get(table);
      if (columns == null) {
        columns = new ArrayList<IntrospectedColumn>();
        answer.put(table, columns);
        if (config.hasProcess()) {
          config.getDatabaseProcess().processTable(table);
        }
      }
      if (config.hasProcess()) {
        config.getDatabaseProcess().processColumn(table, column);
      }
      columns.add(column);
    }
    closeResultSet(rs);
    return answer;
  }

  /**
   * 处理表
   *
   * @param config
   * @param columns
   * @return
   * @throws SQLException
   */
  protected List<IntrospectedTable> calculateIntrospectedTables(
      DatabaseConfig config,
      Map<IntrospectedTable, List<io.mybatis.database.IntrospectedColumn>> columns) throws SQLException {
    List<io.mybatis.database.IntrospectedTable> answer = new ArrayList<io.mybatis.database.IntrospectedTable>();
    //获取表注释信息
    Map<String, String> tableCommentsMap = getTableComments(config);
    Map<String, Map<String, String>> tableColumnCommentsMap = getColumnComments(config);
    for (Map.Entry<io.mybatis.database.IntrospectedTable, List<io.mybatis.database.IntrospectedColumn>> entry : columns
        .entrySet()) {
      IntrospectedTable table = entry.getKey();
      if (tableCommentsMap != null && tableCommentsMap.containsKey(table.getName())) {
        table.setRemarks(tableCommentsMap.get(table.getName()));
      }
      Map<String, String> columnCommentsMap = null;
      if (tableColumnCommentsMap != null && tableColumnCommentsMap.containsKey(table.getName())) {
        columnCommentsMap = tableColumnCommentsMap.get(table.getName());
      }
      for (IntrospectedColumn introspectedColumn : entry.getValue()) {
        FullyQualifiedJavaType fullyQualifiedJavaType = calculateJavaType(introspectedColumn);
        if (fullyQualifiedJavaType != null) {
          introspectedColumn.setFullyQualifiedJavaType(fullyQualifiedJavaType);
          introspectedColumn.setJdbcTypeName(calculateJdbcTypeName(introspectedColumn));
        }
        //转换为驼峰形式
        if (useCamelCase) {
          introspectedColumn.setJavaProperty(JavaBeansUtil.getCamelCaseString(introspectedColumn.getName(), false));
        } else {
          introspectedColumn.setJavaProperty(JavaBeansUtil.getValidPropertyName(introspectedColumn.getName()));
        }
        //处理注释
        if (columnCommentsMap != null && columnCommentsMap.containsKey(introspectedColumn.getName())) {
          introspectedColumn.setRemarks(columnCommentsMap.get(introspectedColumn.getName()));
        }
        table.addColumn(introspectedColumn);
      }
      calculatePrimaryKey(config, table);
      answer.add(table);
    }
    return answer;
  }

  /**
   * 获取表名和注释映射
   *
   * @param config
   * @return
   * @throws SQLException
   */
  protected Map<String, String> getTableComments(io.mybatis.database.DatabaseConfig config) throws SQLException {
    ResultSet rs = dbMetadataUtils.getDatabaseMetaData().getTables(config.getCatalog(), config.getSchemaPattern(), config.getTableNamePattern(), null);
    Map<String, String> answer = new HashMap<String, String>();
    while (rs.next()) {
      answer.put(rs.getString("TABLE_NAME"), rs.getString("REMARKS"));
    }
    closeResultSet(rs);
    return answer;
  }

  /**
   * 获取表名和列名-注释映射
   *
   * @param config
   * @return
   * @throws SQLException
   */
  protected Map<String, Map<String, String>> getColumnComments(DatabaseConfig config) throws SQLException {
    return null;
  }

  public static class JdbcTypeInformation {
    private final String jdbcTypeName;

    private final FullyQualifiedJavaType fullyQualifiedJavaType;

    public JdbcTypeInformation(String jdbcTypeName,
                               FullyQualifiedJavaType fullyQualifiedJavaType) {
      this.jdbcTypeName = jdbcTypeName;
      this.fullyQualifiedJavaType = fullyQualifiedJavaType;
    }

    public String getJdbcTypeName() {
      return jdbcTypeName;
    }

    public FullyQualifiedJavaType getFullyQualifiedJavaType() {
      return fullyQualifiedJavaType;
    }
  }
}
