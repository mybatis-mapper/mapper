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

import io.mybatis.common.util.Utils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 数据源
 */
public final class SimpleDataSource implements DataSource {
  private final Dialect    dialect;
  private final DataSource delegate;
  private       String     url;
  private       String     user;
  private       String     pwd;

  public SimpleDataSource(Dialect dialect, DataSource dataSource) {
    this.dialect = dialect;
    this.delegate = dataSource;
    if (Utils.isNotEmpty(dialect.getDriver())) {
      try {
        Class.forName(dialect.getDriver());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("找不到指定的数据库驱动:" + dialect.getDriver());
      }
    }
  }

  public SimpleDataSource(Dialect dialect, String url, String user, String pwd) {
    this.dialect = dialect;
    this.url = url;
    this.user = user;
    this.pwd = pwd;
    this.delegate = this;
    if (Utils.isNotEmpty(dialect.getDriver())) {
      try {
        Class.forName(dialect.getDriver());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("找不到指定的数据库驱动:" + dialect.getDriver());
      }
    }
  }

  public Dialect getDialect() {
    return dialect;
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  @Override
  public Connection getConnection() throws SQLException {
    if (delegate instanceof SimpleDataSource) {
      return DriverManager.getConnection(url, user, pwd);
    } else {
      return delegate.getConnection();
    }
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    if (delegate instanceof SimpleDataSource) {
      return DriverManager.getConnection(url, username, password);
    } else {
      return delegate.getConnection(username, password);
    }
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {

  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {

  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
