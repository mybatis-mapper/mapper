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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntrospectedTable extends IntrospectedBase {
  protected List<IntrospectedColumn> primaryKeyColumns;
  protected List<IntrospectedColumn> baseColumns;
  private   String                   schema;
  private   String                   catalog;

  public IntrospectedTable() {
    super();
    primaryKeyColumns = new ArrayList<IntrospectedColumn>();
    baseColumns = new ArrayList<IntrospectedColumn>();
  }

  public IntrospectedTable(String catalog, String schema, String name) {
    this();
    this.catalog = catalog;
    this.schema = schema;
    this.name = name;
  }

  public IntrospectedColumn getColumn(String columnName) {
    if (columnName == null) {
      return null;
    } else {
      for (IntrospectedColumn introspectedColumn : baseColumns) {
        if (introspectedColumn.isColumnNameDelimited()) {
          if (introspectedColumn.getName().equals(
              columnName)) {
            return introspectedColumn;
          }
        } else {
          if (introspectedColumn.getName()
              .equalsIgnoreCase(columnName)) {
            return introspectedColumn;
          }
        }
      }
      return null;
    }
  }

  public boolean hasJDBCDateColumns() {
    boolean rc = false;
    if (!rc) {
      for (IntrospectedColumn introspectedColumn : baseColumns) {
        if (introspectedColumn.isJDBCDateColumn()) {
          rc = true;
          break;
        }
      }
    }
    return rc;
  }

  public boolean hasJDBCTimeColumns() {
    boolean rc = false;
    if (!rc) {
      for (IntrospectedColumn introspectedColumn : baseColumns) {
        if (introspectedColumn.isJDBCTimeColumn()) {
          rc = true;
          break;
        }
      }
    }
    return rc;
  }

  public List<IntrospectedColumn> getPrimaryKeyColumns() {
    return primaryKeyColumns;
  }

  public boolean hasPrimaryKeyColumns() {
    return primaryKeyColumns.size() > 0;
  }

  public List<IntrospectedColumn> getBaseColumns() {
    return baseColumns;
  }

  public List<IntrospectedColumn> getAllColumns() {
    return baseColumns;
  }

  public boolean hasBaseColumns() {
    return baseColumns.size() > 0;
  }

  public boolean hasAnyColumns() {
    return baseColumns.size() > 0;
  }

  public void addColumn(IntrospectedColumn introspectedColumn) {
    baseColumns.add(introspectedColumn);
    introspectedColumn.setIntrospectedTable(this);
  }

  public void addPrimaryKeyColumn(String columnName) {
    Iterator<IntrospectedColumn> iter = baseColumns.iterator();
    while (iter.hasNext()) {
      IntrospectedColumn introspectedColumn = iter.next();
      if (introspectedColumn.getName().equals(columnName)) {
        introspectedColumn.setPk(true);
        primaryKeyColumns.add(introspectedColumn);
        break;
      }
    }
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IntrospectedTable)) return false;

    IntrospectedTable that = (IntrospectedTable) o;

    if (catalog != null ? !catalog.equals(that.catalog) : that.catalog != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    return schema != null ? schema.equals(that.schema) : that.schema == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (schema != null ? schema.hashCode() : 0);
    result = 31 * result + (catalog != null ? catalog.hashCode() : 0);
    return result;
  }
}
