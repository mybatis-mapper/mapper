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

package io.mybatis.generator.project.db;

import io.mybatis.generator.project.Base;

import java.util.List;
import java.util.Map;

/**
 * 数据库相关的公共配置
 *
 * @author liuzh
 */
public class Database extends Base {
  /**
   * 区分大小写
   */
  private boolean             caseSensitive;
  /**
   * 前置分隔符
   */
  private String              beginningDelimiter;
  /**
   * 后置分隔符
   */
  private String              endingDelimiter;
  /**
   * 数据库连接
   */
  private JdbcConnection      jdbcConnection;
  /**
   * 要生成的表信息
   */
  private List<Table>         tables;
  /**
   * 主键 - 全局配置
   */
  private TableKey            tableKey;
  /**
   * 关键字
   */
  private List<String>        keywords;
  /**
   * 类型转换
   * <p>
   * java.lang.String: char, varchar, ...
   * java.lang.Long: bigint
   */
  private Map<String, String> typeMap;

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public String getBeginningDelimiter() {
    return beginningDelimiter;
  }

  public void setBeginningDelimiter(String beginningDelimiter) {
    this.beginningDelimiter = beginningDelimiter;
  }

  public String getEndingDelimiter() {
    return endingDelimiter;
  }

  public void setEndingDelimiter(String endingDelimiter) {
    this.endingDelimiter = endingDelimiter;
  }

  public JdbcConnection getJdbcConnection() {
    return jdbcConnection;
  }

  public void setJdbcConnection(JdbcConnection jdbcConnection) {
    this.jdbcConnection = jdbcConnection;
  }

  public List<Table> getTables() {
    return tables;
  }

  public void setTables(List<Table> tables) {
    this.tables = tables;
  }

  public TableKey getTableKey() {
    return tableKey;
  }

  public void setTableKey(TableKey tableKey) {
    this.tableKey = tableKey;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public Map<String, String> getTypeMap() {
    return typeMap;
  }

  public void setTypeMap(Map<String, String> typeMap) {
    this.typeMap = typeMap;
  }
}
