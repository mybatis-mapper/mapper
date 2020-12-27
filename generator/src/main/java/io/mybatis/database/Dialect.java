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
 * 数据库 - 驱动和连接示例
 */
public enum Dialect {
  DB2("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://localhost:50000/SAMPLE"),
  HSQLDB("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:sample"),
  MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://localhost:3306/sample"),
  MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/sample"),
  MYSQL8("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/sample"),
  ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@//localhost:1521/orcl"),
  POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/sample"),
  SQLSERVER("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://localhost:1433/tempdb"),
  /**
   * 纯 JDBC 方式获取数据库信息
   */
  JDBC("", "jdbc:xxx://localhost:port/db"),
  /**
   * 使用 schema 过滤数据库名
   */
  JDBC_SCHEMA("", "jdbc:xxx://localhost:port/db"),
  /**
   * 使用 catalog 过滤数据库名
   */
  JDBC_CATALOG("", "jdbc:xxx://localhost:port/db");

  private final String clazz;
  private final String sample;

  Dialect(String clazz, String sample) {
    this.clazz = clazz;
    this.sample = sample;
  }

  public String getSample() {
    return sample;
  }

  public String getDriver() {
    return clazz;
  }

  /**
   * 驱动是否存在
   *
   * @return
   */
  public boolean exists() {
    try {
      Class.forName(clazz);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
