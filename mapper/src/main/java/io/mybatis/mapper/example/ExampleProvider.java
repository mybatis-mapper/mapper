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

package io.mybatis.mapper.example;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

/**
 * 基础的增删改查操作
 *
 * @author liuzh
 */
public class ExampleProvider {
  // @formatter:off
  private static final String EXAMPLE_SET_CLAUSE_INNER_WHEN =
          "<set>" +
          "  <foreach collection=\"example.setValues\" item=\"setValue\">\n" +
          "    <choose>\n" +
          "      <when test=\"setValue.noValue\">\n" +
          "        ${setValue.condition},\n" +
          "      </when>\n" +
          "      <when test=\"setValue.singleValue\">\n" +
          "        ${setValue.condition} = ${setValue.variables('setValue', 'value')},\n" +
          "      </when>\n" +
          "    </choose>\n" +
          "  </foreach>\n" +
          "</set>";

  private static final String EXAMPLE_WHERE_CLAUSE_INNER_WHEN =
          "            <when test=\"criterion.noValue\">\n" +
          "              AND ${criterion.condition}\n" +
          "            </when>\n" +
          "            <when test=\"criterion.singleValue\">\n" +
          "              AND ${criterion.condition} ${criterion.variables('criterion', 'value')}\n" +
          "            </when>\n" +
          "            <when test=\"criterion.betweenValue\">\n" +
          "              AND ${criterion.condition} ${criterion.variables('criterion', 'value')} AND\n" +
          "              ${criterion.variables('criterion', 'secondValue')}\n" +
          "            </when>\n" +
          "            <when test=\"criterion.listValue\">\n" +
          "              AND ${criterion.condition}\n" +
          "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\"\n" +
          "                open=\"(\" separator=\",\">\n" +
          "                ${criterion.variables(null, 'listItem')}\n" +
          "              </foreach>\n" +
          "            </when>\n";

  /**
   * example 结构的动态 SQL 查询条件，用于接口参数只有一个 Example 对象时
   */
  public static final String EXAMPLE_WHERE_CLAUSE = "<where>\n" +
      "  <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\" OR \">\n" +
      "    <if test=\"criteria.valid\">\n" +
      "      <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n" +
      "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
      "          <choose>\n" +
                   EXAMPLE_WHERE_CLAUSE_INNER_WHEN +
      "            <when test=\"criterion.orValue\">\n" +
      "              <foreach collection=\"criterion.value\" item=\"orCriteria\" separator=\" OR \" open = \" AND (\" close = \")\">\n" +
      "                <if test=\"orCriteria.valid\">\n" +
      "                  <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n" +
      "                    <foreach collection=\"orCriteria.criteria\" item=\"criterion\">\n" +
      "                      <choose>\n" +
                               EXAMPLE_WHERE_CLAUSE_INNER_WHEN +
      "                      </choose>\n" +
      "                    </foreach>\n" +
      "                  </trim>\n" +
      "                </if>\n" +
      "              </foreach>\n" +
      "            </when>\n" +
      "          </choose>\n" +
      "        </foreach>\n" +
      "      </trim>\n" +
      "    </if>\n" +
      "  </foreach>\n" +
      "</where>\n";

  /**
   * example 结构的动态 SQL 查询条件，用于多个参数时，Example 对应 @Param("example")
   */
  public static final String UPDATE_BY_EXAMPLE_WHERE_CLAUSE = "<where>\n" +
      "  <foreach collection=\"example.oredCriteria\" item=\"criteria\"\n separator=\" OR \">\n" +
      "    <if test=\"criteria.valid\">\n" +
      "      <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n" +
      "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
      "          <choose>\n" +
                   EXAMPLE_WHERE_CLAUSE_INNER_WHEN +
      "            <when test=\"criterion.orValue\">\n" +
      "              <foreach collection=\"criterion.value\" item=\"orCriteria\" separator=\" OR \" open = \" AND (\" close = \")\">\n" +
      "                <if test=\"orCriteria.valid\">\n" +
      "                  <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n" +
      "                    <foreach collection=\"orCriteria.criteria\" item=\"criterion\">\n" +
      "                      <choose>\n" +
                               EXAMPLE_WHERE_CLAUSE_INNER_WHEN +
      "                      </choose>\n" +
      "                    </foreach>\n" +
      "                  </trim>\n" +
      "                </if>\n" +
      "              </foreach>\n" +
      "            </when>\n" +
      "          </choose>\n" +
      "        </foreach>\n" +
      "      </trim>\n" +
      "    </if>\n" +
      "  </foreach>\n" +
      "</where>\n";
  // @formatter:on

  /**
   * 根据 Example 删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, (entity, util) ->
        util.ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "DELETE FROM " + entity.tableName()
            + util.parameterNotNull("Example cannot be null")
            //是否允许空条件，默认允许，允许时不检查查询条件
            + (entity.getPropBoolean("deleteByExample.allowEmpty", true) ?
            "" : util.variableIsFalse("_parameter.isEmpty()", "Example Criteria cannot be empty"))
            + EXAMPLE_WHERE_CLAUSE
            + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}"));
  }

  /**
   * 根据 Example 条件批量更新实体信息
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("example.startSql != null and example.startSql != ''", () -> "${example.startSql}")
            + "UPDATE " + entity.tableName()
            + set(() -> entity.updateColumns().stream().map(
            column -> column.columnEqualsProperty("entity.")).collect(Collectors.joining(",")))
            //TODO 测试
            + variableNotNull("example", "Example cannot be null")
            //是否允许空条件，默认允许，允许时不检查查询条件
            + (entity.getPropBoolean("updateByExample.allowEmpty", true) ?
            "" : variableIsFalse("example.isEmpty()", "Example Criteria cannot be empty"))
            + UPDATE_BY_EXAMPLE_WHERE_CLAUSE
            + ifTest("example.endSql != null and example.endSql != ''", () -> "${example.endSql}");
      }
    });
  }

  /**
   * 根据 Example 条件批量更新实体信息
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByExampleSetValues(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("example.startSql != null and example.startSql != ''", () -> "${example.startSql}")
            + variableNotEmpty("example.setValues", "Example setValues cannot be empty")
            + "UPDATE " + entity.tableName()
            + EXAMPLE_SET_CLAUSE_INNER_WHEN
            + variableNotNull("example", "Example cannot be null")
            //是否允许空条件，默认允许，允许时不检查查询条件
            + (entity.getPropBoolean("updateByExample.allowEmpty", true) ?
            "" : variableIsFalse("example.isEmpty()", "Example Criteria cannot be empty"))
            + UPDATE_BY_EXAMPLE_WHERE_CLAUSE
            + ifTest("example.endSql != null and example.endSql != ''", () -> "${example.endSql}");
      }
    });
  }

  /**
   * 根据 Example 条件批量更新实体不为空的字段
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByExampleSelective(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("example.startSql != null and example.startSql != ''", () -> "${example.startSql}")
            + "UPDATE " + entity.tableName()
            + set(() -> entity.updateColumns().stream().map(
            column -> ifTest(column.notNullTest("entity."),
                () -> column.columnEqualsProperty("entity.") + ",")).collect(Collectors.joining(LF)))
            //TODO 测试
            + variableNotNull("example", "Example cannot be null")
            //是否允许空条件，默认允许，允许时不检查查询条件
            + (entity.getPropBoolean("updateByExampleSelective.allowEmpty", true) ?
            "" : variableIsFalse("example.isEmpty()", "Example Criteria cannot be empty"))
            + UPDATE_BY_EXAMPLE_WHERE_CLAUSE
            + ifTest("example.endSql != null and example.endSql != ''", () -> "${example.endSql}");
      }
    });
  }

  /**
   * 根据 Example 条件批量查询，根据 Example 条件查询总数，查询结果的数量由方法定义
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT "
            + ifTest("distinct", () -> "distinct ")
            + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
            + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
            + " FROM " + entity.tableName()
            + ifParameterNotNull(() -> EXAMPLE_WHERE_CLAUSE)
            + ifTest("orderByClause != null", () -> " ORDER BY ${orderByClause}")
            + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(""))
            + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
      }
    });
  }

  /**
   * 根据 Example 条件查询总数
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String countByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT COUNT("
            + ifTest("distinct", () -> "distinct ")
            + ifTest("simpleSelectColumns != null and simpleSelectColumns != ''", () -> "${simpleSelectColumns}")
            + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*")
            + ") FROM "
            + entity.tableName()
            + ifParameterNotNull(() -> EXAMPLE_WHERE_CLAUSE)
            + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
      }
    });
  }

}
