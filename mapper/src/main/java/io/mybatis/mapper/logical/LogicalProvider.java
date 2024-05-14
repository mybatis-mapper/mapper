package io.mybatis.mapper.logical;

import io.mybatis.common.util.Assert;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.List;
import java.util.stream.Collectors;

import static io.mybatis.mapper.example.ExampleProvider.EXAMPLE_WHERE_CLAUSE;
import static io.mybatis.mapper.example.ExampleProvider.UPDATE_BY_EXAMPLE_WHERE_CLAUSE;

/**
 * 支持逻辑删除的provider实现
 * <p>NOTE: 使用时需要在实体类字段上声明@LogicalColumn注解</p>
 *
 * @author hzw
 */
public class LogicalProvider {

  private interface LogicalSqlScript extends SqlScript {
    // TODO: 复用ExampleProvider中的常量，将此常量去除
    String EXAMPLE_SET_CLAUSE_INNER_WHEN =
        "<set>" +
            "  <foreach collection=\"example.setValues\" item=\"setValue\">\n" +
            "    <choose>\n" +
            "      <when test=\"setValue.noValue\">\n" +
            "        ${setValue.condition},\n" +
            "      </when>\n" +
            "      <when test=\"setValue.singleValue\">\n" +
            "        ${setValue.condition} = #{setValue.value},\n" +
            "      </when>\n" +
            "    </choose>\n" +
            "  </foreach>\n" +
            "</set>";

    default String logicalNotEqualCondition(EntityTable entity) {
      EntityColumn logicalColumn = getLogicalColumn(entity);
      return " AND " + columnNotEqualsValueCondition(logicalColumn, deleteValue(logicalColumn)) + LF;
    }
  }

  /* select +++ */

  public static String select(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
            + " FROM " + entity.tableName()
            + where(() -> entity.whereColumns().stream()
            .map(column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
            .collect(Collectors.joining(LF)) + logicalNotEqualCondition(entity))
            + entity.groupByColumn().orElse("")
            + entity.havingColumn().orElse("")
            + entity.orderByColumn().orElse("");
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT "
            + choose(() -> whenTest("fns != null and fns.isNotEmpty()", () -> "${fns.baseColumnAsPropertyList()}")
            + otherwise(() -> entity.baseColumnAsPropertyList()))
            + " FROM " + entity.tableName()
            + trim("WHERE", "", "WHERE |OR |AND ", "", () ->
            ifParameterNotNull(() -> where(() -> entity.whereColumns().stream()
                .map(column -> ifTest(column.notNullTest("entity."), () -> "AND " + column.columnEqualsProperty("entity.")))
                .collect(Collectors.joining(LF))))
                + logicalNotEqualCondition(entity))
            + entity.groupByColumn().orElse("")
            + entity.havingColumn().orElse("")
            + entity.orderByColumn().orElse("");
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT "
            + ifTest("distinct", () -> "distinct ")
            + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
            + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
            + " FROM " + entity.tableName()
            + trim("WHERE", "", "WHERE |OR |AND ", "", () -> ifParameterNotNull(() -> EXAMPLE_WHERE_CLAUSE) + logicalNotEqualCondition(entity))
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return ifTest("startSql != null and startSql != ''", () -> "${startSql}")
            + "SELECT COUNT("
            + ifTest("distinct", () -> "distinct ")
            + ifTest("simpleSelectColumns != null and simpleSelectColumns != ''", () -> "${simpleSelectColumns}")
            + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*")
            + ") FROM "
            + entity.tableName()
            + trim("WHERE", "", "WHERE |OR |AND ", "", () -> ifParameterNotNull(() -> EXAMPLE_WHERE_CLAUSE) + logicalNotEqualCondition(entity))
            + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
      }
    });
  }

  /**
   * 根据主键查找未被逻辑删除的值
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
            + " FROM " + entity.tableName()
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")))
            // 如果将条件拼接where()中，将会依赖idColumns()的实现，要求其必须返回非空值
            + logicalNotEqualCondition(entity);
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT COUNT(*)  FROM " + entity.tableName() + LF
            + where(() ->
            entity.whereColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty())
            ).collect(Collectors.joining(LF)) + logicalNotEqualCondition(entity));
      }
    });
  }

  /* select --- */

  /* update +++ */

  /**
   * 根据 Example 条件批量更新实体信息
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
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
            + trim("WHERE", "", "WHERE |OR |AND ", "", () -> UPDATE_BY_EXAMPLE_WHERE_CLAUSE + logicalNotEqualCondition(entity))
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
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
            + trim("WHERE", "", "WHERE |OR |AND ", "", () -> UPDATE_BY_EXAMPLE_WHERE_CLAUSE + logicalNotEqualCondition(entity))
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
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
            + trim("WHERE", "", "WHERE |OR |AND ", "", () -> UPDATE_BY_EXAMPLE_WHERE_CLAUSE + logicalNotEqualCondition(entity))
            + ifTest("example.endSql != null and example.endSql != ''", () -> "${example.endSql}");
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + " SET " + entity.updateColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(","))
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")))
            + logicalNotEqualCondition(entity);
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
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + set(() ->
            entity.updateColumns().stream().map(column ->
                ifTest(column.notNullTest(), () -> column.columnEqualsProperty() + ",")
            ).collect(Collectors.joining(LF)))
            + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")))
            + logicalNotEqualCondition(entity);
      }
    });
  }

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否null，都更新
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {

      @Override
      public String getSql(EntityTable entity) {
        return "UPDATE " + entity.tableName()
            + set(() ->
            entity.updateColumns().stream().map(column ->
                choose(() ->
                    whenTest("fns != null and fns.fieldNames().contains('" + column.property() + "')", () -> column.columnEqualsProperty("entity.") + ",")
                        + whenTest(column.notNullTest("entity."), () -> column.columnEqualsProperty("entity.") + ","))
            ).collect(Collectors.joining(LF)))
            + where(() -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity.")).collect(Collectors.joining(" AND ")))
            + logicalNotEqualCondition(entity);
      }
    });
  }

  /* update --- */

  /* delete +++ */

  /**
   * 根据实体信息批量删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String delete(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        EntityColumn logicColumn = getLogicalColumn(entity);
        return "UPDATE " + entity.tableName()
            + " SET " + columnEqualsValue(logicColumn, deleteValue(logicColumn))
            + parameterNotNull("Parameter cannot be null")
            + where(() -> entity.columns().stream()
            .map(column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
            .collect(Collectors.joining(LF)) + logicalNotEqualCondition(entity));
      }
    });
  }

  /**
   * 根据主键逻辑删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new LogicalSqlScript() {
          @Override
          public String getSql(EntityTable entity) {
            EntityColumn logicColumn = getLogicalColumn(entity);
            return "UPDATE " + entity.tableName()
                + " SET " + columnEqualsValue(logicColumn, deleteValue(logicColumn))
                + " WHERE " + entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND "))
                + logicalNotEqualCondition(entity);
          }
        }
    );
  }

  /**
   * 根据 Example 删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByExample(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, (entity, util) -> {
      EntityColumn logicColumn = getLogicalColumn(entity);
      return util.ifTest("startSql != null and startSql != ''", () -> "${startSql}")
          + "UPDATE " + entity.tableName()
          + " SET " + columnEqualsValue(logicColumn, deleteValue(logicColumn))
          + util.parameterNotNull("Example cannot be null")
          //是否允许空条件，默认允许，允许时不检查查询条件
          + (entity.getPropBoolean("deleteByExample.allowEmpty", true) ?
          "" : util.variableIsFalse("_parameter.isEmpty()", "Example Criteria cannot be empty"))
          + EXAMPLE_WHERE_CLAUSE + " AND " + columnNotEqualsValueCondition(logicColumn, deleteValue(logicColumn))
          + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}");
    });
  }

  /* delete --- */

  private static EntityColumn getLogicalColumn(EntityTable entity) {
    List<EntityColumn> logicColumns = entity.columns().stream().filter(c -> c.field().isAnnotationPresent(LogicalColumn.class)).collect(Collectors.toList());
    Assert.isTrue(logicColumns.size() == 1, "There are no or multiple fields marked with @LogicalColumn");
    return logicColumns.get(0);
  }

  private static String deleteValue(EntityColumn logicColumn) {
    return logicColumn.field().getAnnotation(LogicalColumn.class).delete();
  }

  private static String columnEqualsValueCondition(EntityColumn c, String value) {
    return " " + c.column() + choiceEqualsOperator(value) + value + " ";
  }

  private static String columnEqualsValue(EntityColumn c, String value) {
    return " " + c.column() + " = " + value + " ";
  }

  private static String columnNotEqualsValueCondition(EntityColumn c, String value) {
    return " " + c.column() + choiceNotEqualsOperator(value) + value;
  }

  private static String choiceEqualsOperator(String value) {
    if ("null".compareToIgnoreCase(value) == 0) {
      return " IS ";
    }
    return " = ";
  }

  private static String choiceNotEqualsOperator(String value) {
    if ("null".compareToIgnoreCase(value) == 0) {
      return " IS NOT ";
    }
    return " != ";
  }
}
