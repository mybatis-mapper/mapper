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

/**
 * 表信息
 *
 * @author liuzh
 */
public class Table extends Base {
  private String   catalog;
  private String   schema;
  /**
   * 实体类名称
   */
  private String   domainName;
  /**
   * Mapper 接口名
   */
  private String   mapperName;
  /**
   * 主键配置
   */
  private TableKey tableKey;

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getMapperName() {
    return mapperName;
  }

  public void setMapperName(String mapperName) {
    this.mapperName = mapperName;
  }

  public TableKey getTableKey() {
    return tableKey;
  }

  public void setTableKey(TableKey tableKey) {
    this.tableKey = tableKey;
  }
}
