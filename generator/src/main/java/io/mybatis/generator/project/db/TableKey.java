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

public class TableKey {

  private String column;

  private String runtimeSqlStatement;

  private boolean identity;

  private String type;

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public String getRuntimeSqlStatement() {
    return runtimeSqlStatement;
  }

  public void setRuntimeSqlStatement(String runtimeSqlStatement) {
    this.runtimeSqlStatement = runtimeSqlStatement;
  }

  public boolean isIdentity() {
    return identity;
  }

  public void setIdentity(boolean identity) {
    this.identity = identity;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
