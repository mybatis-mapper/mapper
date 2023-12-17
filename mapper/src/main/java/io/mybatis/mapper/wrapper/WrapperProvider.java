package io.mybatis.mapper.wrapper;

import io.mybatis.mapper.wrapper.logical.LogicDeleteUtil;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.stream.Collectors;

/**
 * 基础的增删改查操作
 *
 * @author dyun
 */
public class WrapperProvider {

  /*
   * ====================================provider for entity ====================================
   */

  /**
   * 获取ID条件： 形式 column = #{property} 形式的字符串
   *
   * @param entity 实体类
   * @return ID条件
   */
  public static String getIdEqualsProperty(EntityTable entity) {
    return entity.idColumns().stream()
        .map(EntityColumn::columnEqualsProperty)
        .collect(Collectors.joining(" AND "));
  }

  public static final String insertInWrapper = "insertInWrapper";

  /**
   * 保存实体（字段不为空）
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String insertInWrapper(ProviderContext providerContext) {
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

  public static final String insertNullableInWrapper = "insertNullableInWrapper";

  /**
   * 保存实体（字段可为空）
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String insertNullableInWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity -> "INSERT INTO " + entity.tableName()
        + "(" + entity.insertColumnList() + ")"
        + " VALUES (" + entity.insertColumns().stream()
        .map(EntityColumn::variables).collect(Collectors.joining(",")) + ")");
  }

  public static final String updateByIdInWrapper = "updateByIdInWrapper";

  /**
   * 根据主键更新实体（字段不为空）
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByIdInWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + set(() ->
            entity.updateColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> column.columnEqualsProperty() + ",")
            ).collect(Collectors.joining(LF)))
            + " WHERE " + getIdEqualsProperty(entity)
            + LogicDeleteUtil.andNotDelete(entity);
      }
    });
  }

  public static final String updateNullableByIdInWrapper = "updateNullableByIdInWrapper";

  /**
   * 根据主键更新实体（字段可为空）
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateNullableByIdInWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + " SET " + entity.updateColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(","))
            + " WHERE " + getIdEqualsProperty(entity)
            + LogicDeleteUtil.andNotDelete(entity);
      }
    });
  }

  public static final String deleteByIdInWrapper = "deleteByIdInWrapper";

  /**
   * 根据主键删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByIdInWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity -> {
      if (LogicDeleteUtil.getLogicColumn(entity) != null) {
        return "UPDATE " + entity.tableName()
            + " SET " + LogicDeleteUtil.setIsDelete(entity)
            + " WHERE " + getIdEqualsProperty(entity);
      }
      return "DELETE FROM " + entity.tableName()
          + " WHERE " + getIdEqualsProperty(entity);
    });
  }

  public static final String selectByIdInWrapper = "selectByIdInWrapper";

  /**
   * 根据主键查询实体
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByIdInWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
            + " FROM " + entity.tableName()
            + where(() -> getIdEqualsProperty(entity)
            + LogicDeleteUtil.andNotDelete(entity));
      }
    });
  }

  /*
   * ====================================provider for wrapper ====================================
   */

  public static final String updateByWrapper = "updateByWrapper";

  /**
   * 根据 Wrapper 条件批量更新实体信息
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return variableNotNull("wrapper", "Wrapper cannot be null")
            + ifTest("wrapper.startSql != null and wrapper.startSql != ''", () -> "${wrapper.startSql}")
            + "UPDATE " + entity.tableName()
            + set(() ->
            setClause("wrapper.") +
                entity.updateColumns().stream().map(column ->
                    ifTest("!wrapper.setValuesContains('" + column.column() + "')", () ->
                        choose(() -> whenTest("wrapper.updateNullable",
                            () -> column.columnEqualsProperty("entity.") + ","))
                            + choose(() -> whenTest("!wrapper.updateNullable and " + column.notNullTest("entity."),
                            () -> column.columnEqualsProperty("entity.") + ",")))
                ).collect(Collectors.joining(LF)))
            // 是否允许空条件，默认允许(不防全表更新或删除)，允许时不检查查询条件
            + (entity.getPropBoolean("updateByWrapper.allowEmpty", true) ?
            "" : variableIsFalse("wrapper.isEmpty()", "Wrapper Criteria cannot be empty"))
            + whereClause("wrapper.", LogicDeleteUtil.andNotDelete(entity))
            + ifTest("wrapper.endSql != null and wrapper.endSql != ''", () -> "${wrapper.endSql}");
      }
    });
  }

  public static final String deleteByWrapper = "deleteByWrapper";

  /**
   * 根据 Wrapper 删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, (entity, util) -> {
      String sql = util.ifTest("startSql != null and startSql != ''", () -> "${startSql}");
      if (LogicDeleteUtil.getLogicColumn(entity) != null) {
        sql = sql + "UPDATE " + entity.tableName() + " SET " + LogicDeleteUtil.setIsDelete(entity);
      } else {
        sql = sql + "DELETE FROM " + entity.tableName();
      }
      return sql
          + util.parameterNotNull("Wrapper cannot be null")
          // 是否允许空条件，默认允许(不防全表更新或删除)，允许时不检查查询条件
          + (entity.getPropBoolean("deleteByWrapper.allowEmpty", true) ?
          "" : util.variableIsFalse("_parameter.isEmpty()", "Wrapper Criteria cannot be empty"))
          + whereClause("", "")
          + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}");
    });
  }

  public static final String selectByWrapper = "selectByWrapper";

  /**
   * 根据 Wrapper 条件批量查询，根据 Wrapper 条件查询总数，查询结果的数量由方法定义
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT "
            + ifTest("distinct", () -> "distinct ")
            + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
            + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
            + " FROM " + entity.tableName()
            + ifParameterNotNull(() -> whereClause("", LogicDeleteUtil.andNotDelete(entity)))
            + ifParameterIsNull(() -> LogicDeleteUtil.whereNotDelete(entity))
            + ifTest("orderByClause != null", () -> " ORDER BY ${orderByClause}")
            + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(""))
            + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
      }
    });
  }

  public static final String countByWrapper = "countByWrapper";

  /**
   * 根据 Wrapper 条件查询总数
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String countByWrapper(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT COUNT(1) "
            + " FROM " + entity.tableName()
            + ifParameterNotNull(() -> whereClause("", LogicDeleteUtil.andNotDelete(entity)))
            + ifParameterIsNull(() -> LogicDeleteUtil.whereNotDelete(entity))
            + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
      }
    });
  }

  /*
   * ==============================================xml fragment start==============================================
   */

  /**
   * 生成 &lt;if test="_parameter != null"&gt; 标签包装的 xml 结构，允许参数为空时使用，
   *
   * @param content 标签中的内容
   * @return &lt;if test="_parameter == null"&gt; 标签包装的 xml 结构
   */
  public static String ifParameterIsNull(SqlScript.LRSupplier content) {
    return String.format("<if test=\"_parameter == null\">%s\n</if> ", content.getWithLR());
  }

  /**
   * Wrapper 结构的动态 SQL 查询条件
   *
   * @param prefix         用于多个参数时， 需传入wrapperName，Wrapper 对应 @Param("wrapper"), 注意需自己拼接 . 条件
   * @param logicDeleteSql 逻辑删除片段 需传入 AND + 逻辑删除条件, 注意需自己拼接AND条件
   * @return xml形式查询条件
   */
  public static String whereClause(String prefix, String logicDeleteSql) {
    String clause = "<where>\n" +
        "  <foreach collection=\"_defaultWrapperName点oredCriteria\" item=\"criteria\" separator=\" OR \">\n" +
        "    <if test=\"criteria.valid\">\n" +
        "      <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n"
        + useCriterions("criteria.")
        + "      </trim>\n" +
        "    </if>\n" +
        "  </foreach>\n" +
        "  <if test=\"_defaultWrapperName点lastCriteria != null and _defaultWrapperName点lastCriteria.valid\">\n"
        + useCriterions("_defaultWrapperName点lastCriteria.")
        + "  </if>\n" +
        "  _defaultLogicDeleteSql_\n" +
        "</where>\n";
    return clause.replace("_defaultWrapperName点", prefix).replace("_defaultLogicDeleteSql_", logicDeleteSql);
  }

  /**
   * 迭代使用 criterion list 条件列表
   */
  public static String useCriterions(String criterionName) {
    return criterions.replace("_defaultCriterionsName点", criterionName);
  }

  /**
   * criterion list 条件列表
   */
  public static final String criterions = "        <foreach collection=\"_defaultCriterionsName点criteria\" item=\"criterion\">\n" +
      "          <choose>\n" +
      "            <when test=\"criterion.noValue\">\n" +
      "              AND ${criterion.condition}\n" +
      "            </when>\n" +
      "            <when test=\"criterion.singleValue\">\n" +
      "              AND ${criterion.condition} #{criterion.value}\n" +
      "            </when>\n" +
      "            <when test=\"criterion.betweenValue\">\n" +
      "              AND ${criterion.condition} #{criterion.value} AND #{criterion.secondValue}\n" +
      "            </when>\n" +
      "            <when test=\"criterion.listValue\">\n" +
      "              AND ${criterion.condition}\n" +
      "              <foreach collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\" close=\")\">\n" +
      "                #{listItem}\n" +
      "              </foreach>\n" +
      "            </when>\n" +
      "            <when test=\"criterion.orValue\">\n" +
      "              <foreach collection=\"criterion.value\" item=\"orCriteria\" separator=\" OR \" open=\" AND (\" close=\")\">\n" +
      "                <if test=\"orCriteria.valid\">\n" +
      "                  <trim prefix=\"(\" prefixOverrides=\"AND\" suffix=\")\">\n" +
      "                    <foreach collection=\"orCriteria.criteria\" item=\"criterion\">\n" +
      "                      <choose>\n" +
      "                        <when test=\"criterion.noValue\">\n" +
      "                          AND ${criterion.condition}\n" +
      "                        </when>\n" +
      "                        <when test=\"criterion.singleValue\">\n" +
      "                          AND ${criterion.condition} #{criterion.value}\n" +
      "                        </when>\n" +
      "                        <when test=\"criterion.betweenValue\">\n" +
      "                          AND ${criterion.condition} #{criterion.value} AND #{criterion.secondValue}\n" +
      "                        </when>\n" +
      "                        <when test=\"criterion.listValue\">\n" +
      "                          AND ${criterion.condition}\n" +
      "                          <foreach collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\" close=\")\">\n" +
      "                            #{listItem}\n" +
      "                          </foreach>\n" +
      "                        </when>\n" +
      "                      </choose>\n" +
      "                    </foreach>\n" +
      "                  </trim>\n" +
      "                </if>\n" +
      "              </foreach>\n" +
      "            </when>\n" +
      "          </choose>\n" +
      "        </foreach>\n";

  /**
   * Wrapper 结构的动态 SQL SET语句
   * 多个参数时， 需传入wrapperName，Wrapper 对应 @Param("wrapper")
   */
  public static String setClause(String prefix) {
    String clause = "<foreach collection=\"_defaultWrapperName点setValues\" item=\"setValue\">\n" +
        "  <choose>\n" +
        "    <when test=\"setValue.noValue\">\n" +
        "      ${setValue.condition},\n" +
        "    </when>\n" +
        "    <when test=\"setValue.singleValue\">\n" +
        "      ${setValue.condition} = #{setValue.value},\n" +
        "    </when>\n" +
        "  </choose>\n" +
        "</foreach>\n";
    return clause.replace("_defaultWrapperName点", prefix);
  }

}
