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

package io.mybatis.provider.extend;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 扩展实体信息
 *
 * @author liuzh
 */
public class ExtendEntityTable extends EntityTable {
  public static final Pattern DELIMITER       = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");
  public static final String  RESULT_MAP_NAME = "BaseProviderResultMap";
  /**
   * 表名，覆盖代理中的名称
   */
  private             String  table;
  /**
   * 使用指定的 &lt;resultMap&gt;
   */
  private             String  resultMap;

  /**
   * 自动根据字段生成 &lt;resultMap&gt;
   */
  private boolean autoResultMap;

  /**
   * 已初始化自动ResultMap
   */
  private List<ResultMap> resultMaps;

  /**
   * 已设置返回值为 resultMap 的方法
   */
  private volatile Map<String, Boolean> setResultMap;

  public ExtendEntityTable(EntityTable delegate) {
    super(delegate);
    init();
  }

  private void init() {
    if (delegate.entityClass().isAnnotationPresent(Extend.Table.class)) {
      Extend.Table extendTable = delegate.entityClass().getAnnotation(Extend.Table.class);
      if (!extendTable.value().isEmpty()) {
        this.table = extendTable.value();
      }
      this.resultMap = extendTable.resultMap();
      this.autoResultMap = extendTable.autoResultMap();
      this.setResultMap = new ConcurrentHashMap<>();
    }
  }

  @Override
  public String table() {
    return (this.table == null || this.table.isEmpty()) ? super.table() : this.table;
  }

  /**
   * 第一次初始化，只执行一次
   *
   * @return true 第一次，false 相反
   */
  protected boolean isFirstInit() {
    return (resultMap != null || autoResultMap) && resultMaps == null;
  }

  /**
   * 是否使用 resultMaps
   *
   * @param providerContext 当前方法信息
   * @param cacheKey        缓存 key，每个方法唯一，默认和 msId 一样
   * @return true 是，false 否
   */
  protected boolean useResultMaps(ProviderContext providerContext, String cacheKey) {
    return resultMaps != null
        && providerContext.getMapperMethod().isAnnotationPresent(SelectProvider.class)
        && (setResultMap != null && !setResultMap.containsKey(cacheKey));
  }

  @Override
  public void initRuntimeContext(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    delegate.initRuntimeContext(configuration, providerContext, cacheKey);
    //初始化一次，后续不会重复初始化
    if (isFirstInit()) {
      initResultMap(configuration, providerContext, cacheKey);
    }
    if (useResultMaps(providerContext, cacheKey)) {
      synchronized (cacheKey) {
        if (!setResultMap.containsKey(cacheKey)) {
          MetaObject metaObject = SystemMetaObject.forObject(configuration.getMappedStatement(cacheKey));
          metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
          setResultMap.put(cacheKey, true);
        }
      }
    }
  }

  protected void initResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    if (resultMap != null && !resultMap.isEmpty()) {
      synchronized (this) {
        if (resultMaps == null) {
          resultMaps = new ArrayList<>();
          String resultMapId = generateResultMapId(providerContext, resultMap);
          if (configuration.hasResultMap(resultMapId)) {
            resultMaps.add(configuration.getResultMap(resultMapId));
          } else if (configuration.hasResultMap(resultMap)) {
            resultMaps.add(configuration.getResultMap(resultMap));
          } else {
            throw new RuntimeException(entityClass().getName() + " configured resultMap: " + resultMap + " not found");
          }
        }
      }
    } else if (autoResultMap) {
      synchronized (this) {
        if (resultMaps == null) {
          resultMaps = new ArrayList<>();
          resultMaps.add(genResultMap(configuration, providerContext, cacheKey));
        }
      }
    }
  }

  protected String generateResultMapId(ProviderContext providerContext, String resultMapId) {
    if (resultMapId.indexOf(".") > 0) {
      return resultMapId;
    }
    return providerContext.getMapperType().getName() + "." + resultMapId;
  }

  protected ResultMap genResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    List<ResultMapping> resultMappings = new ArrayList<>();
    for (EntityColumn entityColumn : selectColumns()) {
      ExtendEntityColumn columnExt = entityColumn.delegate(ExtendEntityColumn.class).get();
      String column = columnExt.column();
      //去掉可能存在的分隔符，例如：`order`
      Matcher matcher = DELIMITER.matcher(column);
      if (matcher.find()) {
        column = matcher.group(1);
      }
      ResultMapping.Builder builder = new ResultMapping.Builder(configuration, columnExt.property(), column, columnExt.javaType());
      if (columnExt.getJdbcType() != null && columnExt.getJdbcType() != JdbcType.UNDEFINED) {
        builder.jdbcType(columnExt.getJdbcType());
      }
      if (columnExt.getTypeHandler() != null && columnExt.getTypeHandler() != UnknownTypeHandler.class) {
        try {
          builder.typeHandler(getTypeHandlerInstance(columnExt.javaType(), columnExt.getTypeHandler()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      List<ResultFlag> flags = new ArrayList<>();
      if (columnExt.isId()) {
        flags.add(ResultFlag.ID);
      }
      builder.flags(flags);
      resultMappings.add(builder.build());
    }
    String resultMapId = generateResultMapId(providerContext, RESULT_MAP_NAME);
    ResultMap.Builder builder = new ResultMap.Builder(configuration, resultMapId, entityClass(), resultMappings, true);
    return builder.build();
  }

  @Override
  public String baseColumnAsPropertyList() {
    //当存在 resultMaps 时，查询列不能用别名
    if (resultMaps != null) {
      return baseColumnList();
    }
    return delegate.baseColumnAsPropertyList();
  }

  /**
   * 实例化TypeHandler
   */
  public TypeHandler getTypeHandlerInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    if (javaTypeClass != null) {
      try {
        Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
        return (TypeHandler) c.newInstance(javaTypeClass);
      } catch (NoSuchMethodException ignored) {
        // ignored
      } catch (Exception e) {
        throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
      }
    }
    try {
      Constructor<?> c = typeHandlerClass.getConstructor();
      return (TypeHandler) c.newInstance();
    } catch (Exception e) {
      throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
    }
  }

  @Override
  public List<EntityColumn> selectColumns() {
    return delegate.selectColumns().stream()
        .filter(column -> column.delegate(ExtendEntityColumn.class).get().selectable())
        .collect(Collectors.toList());
  }

  @Override
  public List<EntityColumn> insertColumns() {
    return delegate.insertColumns().stream()
        .filter(column -> column.delegate(ExtendEntityColumn.class).get().insertable())
        .collect(Collectors.toList());
  }

  @Override
  public List<EntityColumn> updateColumns() {
    return delegate.updateColumns().stream()
        .filter(column -> column.delegate(ExtendEntityColumn.class).get().updatable())
        .collect(Collectors.toList());
  }

  @Override
  public Optional<List<EntityColumn>> orderByColumns() {
    Optional<List<EntityColumn>> orderByColumnsOptional = delegate.orderByColumns();
    List<EntityColumn> orderByColumns = orderByColumnsOptional.orElseGet(ArrayList::new);
    delegate.columns().stream().filter(column -> ((ExtendEntityColumn) column).orderBy() != null
        && !((ExtendEntityColumn) column).orderBy().isEmpty()
        && !orderByColumns.contains(column)).forEach(orderByColumns::add);
    if (orderByColumns.size() > 0) {
      return Optional.of(orderByColumns);
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> orderByColumnList() {
    Optional<List<EntityColumn>> orderByColumns = orderByColumns();
    return orderByColumns.map(entityColumns -> entityColumns.stream()
        .map(column -> column.column() + " " + ((ExtendEntityColumn) column).orderBy())
        .collect(Collectors.joining(",")));
  }

  /**
   * 使用指定的 &lt;resultMap&gt;
   */
  public void setResultMap(String resultMap) {
    this.resultMap = resultMap;
  }

  /**
   * 自动根据字段生成 &lt;resultMap&gt;
   */
  public void setAutoResultMap(boolean autoResultMap) {
    this.autoResultMap = autoResultMap;
  }
}
