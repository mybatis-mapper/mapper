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

package io.mybatis.mapper.fn;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

/**
 * 可指定字段的方法
 *
 * @author liuzh
 */
public class FnProvider {

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否null，都更新
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName("entity.")
            + set(() ->
            entity.updateColumns().stream().map(column ->
                choose(() ->
                    whenTest("fns != null and fns.fieldNames().contains('" + column.property() + "')", () -> column.columnEqualsProperty("entity.") + ",")
                        + whenTest(column.notNullTest("entity."), () -> column.columnEqualsProperty("entity.") + ","))

            ).collect(Collectors.joining(LF)))
            + where(() -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity.")).collect(Collectors.joining(" AND ")));
      }
    });
  }

  /**
   * 根据实体字段条件查询唯一的实体，根据实体字段条件批量查询，查询结果的数量由方法定义
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectColumns(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT "
            + choose(() -> whenTest("fns != null and fns.isNotEmpty()", () -> "${fns.baseColumnAsPropertyList()}")
            + otherwise(() -> entity.baseColumnAsPropertyList()))
            + (entity.getProp("dynamicTable.return", true) ? entity.dynamicTableNameVarReturn("entity.") : "")
            + " FROM " + entity.tableName("entity.")
            + ifParameterNotNull(() ->
            where(() ->
                entity.whereColumns().stream().map(column ->
                    ifTest(column.notNullTest("entity."), () -> "AND " + column.columnEqualsProperty("entity."))
                ).collect(Collectors.joining(LF)))
        )
            + entity.groupByColumn().orElse("")
            + entity.havingColumn().orElse("")
            + entity.orderByColumn().orElse("");
      }
    });
  }

}
