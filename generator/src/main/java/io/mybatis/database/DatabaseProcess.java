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

import java.util.List;

/**
 * 内省数据库时的处理接口
 */
public interface DatabaseProcess {
  /**
   * 开始处理
   */
  void processStart();

  /**
   * 处理字段
   *
   * @param table
   * @param column
   */
  void processColumn(IntrospectedTable table, IntrospectedColumn column);

  /**
   * 处理表
   *
   * @param table
   */
  void processTable(IntrospectedTable table);

  /**
   * 处理完成
   *
   * @param introspectedTables
   */
  void processComplete(List<IntrospectedTable> introspectedTables);
}
