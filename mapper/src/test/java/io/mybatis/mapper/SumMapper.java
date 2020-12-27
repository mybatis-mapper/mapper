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

package io.mybatis.mapper;

import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.Caching;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

public interface SumMapper<T> {

  /**
   * 根据 entity 查询条件，查询 sum(column) 总数
   *
   * @param column 指定的查询列
   * @param entity 查询条件
   * @return 总数
   */
  @Lang(Caching.class)
  @SelectProvider(type = SumMapperProvider.class, method = "sum")
  long sum(@Param("column") Fn<T, ? extends Number> column, @Param("entity") T entity);

  class SumMapperProvider {
    public static String sum(ProviderContext providerContext) {
      return SqlScript.caching(providerContext, new SqlScript() {
        @Override
        public String getSql(EntityTable entity) {
          return "SELECT SUM(${column.toColumn()}) FROM " + entity.table()
              + ifTest("entity != null", () ->
              where(() ->
                  entity.whereColumns().stream().map(column ->
                      ifTest(column.notNullTest("entity."), () -> "AND " + column.columnEqualsProperty("entity."))
                  ).collect(Collectors.joining(LF)))
          );
        }
      });
    }
  }
}
