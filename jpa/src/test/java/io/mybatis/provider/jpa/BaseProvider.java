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

package io.mybatis.provider.jpa;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

import static io.mybatis.provider.SqlScript.LF;

public class BaseProvider {

  public static String getById(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity ->
        "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName() +
            " WHERE " + entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
  }

  public static String deleteById(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity ->
        "DELETE FROM " + entity.tableName() +
            " WHERE " + entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
  }

  public static String insertSelective(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, (entity, util) ->
        "INSERT INTO " + entity.tableName()
            + util.trimSuffixOverrides("(", ")", ",", () ->
            entity.insertColumns().stream().map(column ->
                util.ifTest(column.notNullTest(), () -> column.column() + ",")
            ).collect(Collectors.joining(LF)))
            + util.trimSuffixOverrides(" VALUES (", ")", ",", () ->
            entity.insertColumns().stream().map(column ->
                util.ifTest(column.notNullTest(), () -> column.variables() + ",")
            ).collect(Collectors.joining(LF))));
  }
}
