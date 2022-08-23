/*
 * Copyright 2020-2022 the original author or authors.
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

package io.mybatis.mapper.base;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

/**
 * 基础的增删改查操作
 *
 * @author liuzh
 */
public class EntityProvider {

  /**
   * 不可用方法
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String unsupported(ProviderContext providerContext) {
    throw new UnsupportedOperationException(providerContext.getMapperMethod().getName() + " method not available");
  }

  /**
   * 保存实体
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String insert(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity -> "INSERT INTO " + entity.tableName()
        + "(" + entity.insertColumnList() + ")"
        + " VALUES (" + entity.insertColumns().stream()
        .map(EntityColumn::variables).collect(Collectors.joining(",")) + ")");
  }

  /**
   * 保存实体中不为空的字段
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String insertSelective(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "INSERT INTO " + entity.tableName()
            + trimSuffixOverrides("(", ")", ",", () ->
            entity.insertColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> column.column() + ",")
            ).collect(Collectors.joining(LF)))
            + trimSuffixOverrides(" VALUES (", ")", ",", () ->
            entity.insertColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> column.variables() + ",")
            ).collect(Collectors.joining(LF)));
      }
    });
  }

  /**
   * 根据主键删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity -> "DELETE FROM " + entity.tableName()
        + " WHERE " + entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
  }

  /**
   * 保存实体信息批量删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String delete(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "DELETE FROM " + entity.tableName()
            + parameterNotNull("Parameter cannot be null")
            + where(() ->
            entity.columns().stream().map(column ->
                ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty())
            ).collect(Collectors.joining(LF)));
      }
    });
  }

  /**
   * 根据主键更新实体
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + " SET " + entity.updateColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(","))
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
      }
    });
  }

  /**
   * 根据主键更新实体中不为空的字段
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + set(() ->
            entity.updateColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> column.columnEqualsProperty() + ",")
            ).collect(Collectors.joining(LF)))
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
      }
    });
  }

  /**
   * 根据主键查询实体
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
            + " FROM " + entity.tableName()
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
      }
    });
  }

  /**
   * 根据实体字段条件查询唯一的实体，根据实体字段条件批量查询，查询结果的数量由方法定义
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String select(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
            + " FROM " + entity.tableName()
            + ifParameterNotNull(() ->
            where(() ->
                entity.whereColumns().stream().map(column ->
                    ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty())
                ).collect(Collectors.joining(LF)))
        )
            + entity.groupByColumn().orElse("")
            + entity.havingColumn().orElse("")
            + entity.orderByColumn().orElse("");
      }
    });
  }

  /**
   * 根据实体字段条件查询总数
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectCount(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT COUNT(*)  FROM " + entity.tableName() + LF
            + ifParameterNotNull(() ->
            where(() ->
                entity.whereColumns().stream().map(column ->
                    ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty())
                ).collect(Collectors.joining(LF)))
        );
      }
    });
  }

}
