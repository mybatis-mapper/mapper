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

package io.mybatis.database;

/**
 * 数据库查询配置
 *
 * @author liuzh
 */
public class DatabaseConfig {
  private String          catalog;
  private String          schemaPattern;
  private String          tableNamePattern;
  private DatabaseProcess databaseProcess;

  public DatabaseConfig() {
    this(null, null);
  }

  public DatabaseConfig(String catalog, String schemaPattern) {
    this(catalog, schemaPattern, "%");
  }

  public DatabaseConfig(String catalog, String schemaPattern, String tableNamePattern) {
    this.catalog = catalog;
    this.schemaPattern = schemaPattern;
    this.tableNamePattern = tableNamePattern;
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getSchemaPattern() {
    return schemaPattern;
  }

  public void setSchemaPattern(String schemaPattern) {
    this.schemaPattern = schemaPattern;
  }

  public String getTableNamePattern() {
    return tableNamePattern;
  }

  public void setTableNamePattern(String tableNamePattern) {
    this.tableNamePattern = tableNamePattern;
  }

  public boolean hasProcess() {
    return databaseProcess != null;
  }

  public DatabaseProcess getDatabaseProcess() {
    return databaseProcess;
  }

  public void setDatabaseProcess(DatabaseProcess databaseProcess) {
    this.databaseProcess = databaseProcess;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DatabaseConfig that = (DatabaseConfig) o;

    if (catalog != null ? !catalog.equals(that.catalog) : that.catalog != null) return false;
    if (schemaPattern != null ? !schemaPattern.equals(that.schemaPattern) : that.schemaPattern != null)
      return false;
    return tableNamePattern != null ? tableNamePattern.equals(that.tableNamePattern) : that.tableNamePattern == null;
  }

  @Override
  public int hashCode() {
    int result = catalog != null ? catalog.hashCode() : 0;
    result = 31 * result + (schemaPattern != null ? schemaPattern.hashCode() : 0);
    result = 31 * result + (tableNamePattern != null ? tableNamePattern.hashCode() : 0);
    return result;
  }
}
