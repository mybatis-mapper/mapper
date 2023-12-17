package io.mybatis.mapper.wrapper.logical;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 逻辑删除工具类
 *
 * @author dyun
 */
public class LogicDeleteUtil {

  /**
   * 已删除, isDelete = 1 或者 isDelete IS NOT NULL
   */
  public static String isDelete(String prefix, EntityTable entity) {
    EntityColumn logicColumn = getLogicColumn(entity);
    if (logicColumn != null) {
      TableLogic annotation = logicColumn.field().getAnnotation(TableLogic.class);
      if (annotation.dateType()) {
        return String.format(" %s %s IS NOT NULL ", prefix, logicColumn.column());
      }
      if (!"".equals(annotation.value())) {
        return String.format(" %s %s = %s ", prefix, logicColumn.column(), annotation.value());
      }
    }
    return "";
  }

  /**
   * 未删除, isDelete != 1 或者 isDelete IS NULL
   */
  public static String notDelete(String prefix, EntityTable entity) {
    EntityColumn logicColumn = getLogicColumn(entity);
    if (logicColumn != null) {
      TableLogic annotation = logicColumn.field().getAnnotation(TableLogic.class);
      if (annotation.dateType()) {
        return String.format(" %s %s IS NULL ", prefix, logicColumn.column());
      }
      if (!"".equals(annotation.value())) {
        return String.format(" %s %s != %s ", prefix, logicColumn.column(), annotation.value());
      }
    }
    return "";
  }

  /**
   * 已删除, isDelete = 1 或者 isDelete IS NOT NULL
   */
  public static String andIsDelete(EntityTable entity) {
    return isDelete("AND", entity);
  }

  /**
   * 未删除, isDelete != 1 或者 isDelete IS NULL
   */
  public static String andNotDelete(EntityTable entity) {
    return notDelete("AND", entity);
  }

  /**
   * 已删除, isDelete = 1 或者 isDelete IS NOT NULL
   */
  public static String whereIsDelete(EntityTable entity) {
    return isDelete("WHERE", entity);
  }

  /**
   * 未删除, isDelete != 1 或者 isDelete IS NULL
   */
  public static String whereNotDelete(EntityTable entity) {
    return notDelete("WHERE", entity);
  }

  /**
   * 设置字段删除, isDelete = 1 或者 isDelete = 2012-12-12 12:12:12
   * <p>
   * 恢复删除请使用update相关接口
   */
  public static String setIsDelete(EntityTable entity) {
    EntityColumn logicColumn = getLogicColumn(entity);
    if (logicColumn != null) {
      TableLogic annotation = logicColumn.field().getAnnotation(TableLogic.class);
      if (annotation.dateType()) {
        return String.format(" %s = %s ", logicColumn.column(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      }
      if (!"".equals(annotation.value())) {
        return String.format(" %s = %s ", logicColumn.column(), annotation.value());
      }
    }
    return "";
  }

  /**
   * 获取逻辑删除字段
   */
  public static EntityColumn getLogicColumn(EntityTable entity) {
    List<EntityColumn> logicColumns = entity.columns().stream().filter(c -> c.field().isAnnotationPresent(TableLogic.class)).collect(Collectors.toList());
    if (logicColumns.size() > 0) {
      return logicColumns.get(0);
    }
    return null;
  }
}

