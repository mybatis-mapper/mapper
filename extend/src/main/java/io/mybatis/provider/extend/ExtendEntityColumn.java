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
import io.mybatis.provider.EntityField;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.Optional;

/**
 * 扩展列信息
 *
 * @author liuzh
 */
public class ExtendEntityColumn extends EntityColumn {
  protected String                       column;
  protected boolean                      id;
  protected String                       orderBy;
  protected boolean                      selectable = true;
  protected boolean                      insertable = true;
  protected boolean                      updatable  = true;
  protected JdbcType                     jdbcType;
  protected Class<? extends TypeHandler> typeHandler;
  protected String                       numericScale;

  public ExtendEntityColumn(EntityColumn delegate) {
    super(delegate);
    init();
  }

  protected void init() {
    EntityField field = field();
    if (field.isAnnotationPresent(Extend.Column.class)) {
      Extend.Column extendColumn = field.getAnnotation(Extend.Column.class);
      this.column = extendColumn.value();
      this.id = extendColumn.id();
      this.orderBy = extendColumn.orderBy();
      this.selectable = extendColumn.selectable();
      this.insertable = extendColumn.insertable();
      this.updatable = extendColumn.updatable();
      this.jdbcType = extendColumn.jdbcType();
      this.typeHandler = extendColumn.typeHandler();
      this.numericScale = extendColumn.numericScale();
    }
  }

  @Override
  public boolean isId() {
    return super.isId() || this.id;
  }

  @Override
  public String column() {
    return (this.column == null || this.column.isEmpty()) ? super.column() : this.column;
  }

  @Override
  public String variables(String prefix) {
    return "#{" + prefix + property()
        + jdbcType().orElse("")
        + typeHandler().orElse("")
        + numericScale().orElse("") + "}";
  }

  /**
   * 数据库类型 {, jdbcType=VARCHAR}
   */
  protected Optional<String> jdbcType() {
    if (this.jdbcType != null && this.jdbcType != JdbcType.UNDEFINED) {
      return Optional.of(", jdbcType=" + jdbcType);
    }
    return Optional.empty();
  }

  /**
   * 类型处理器 {, typeHandler=XXTypeHandler}
   */
  protected Optional<String> typeHandler() {
    if (this.typeHandler != null && this.typeHandler != UnknownTypeHandler.class) {
      return Optional.of(", typeHandler=" + typeHandler.getName());
    }
    return Optional.empty();
  }

  /**
   * 小数位数 {, numericScale=2}
   */
  protected Optional<String> numericScale() {
    if (this.numericScale != null && !this.numericScale.isEmpty()) {
      return Optional.of(", numericScale=" + numericScale);
    }
    return Optional.empty();
  }

  /**
   * 排序方式，默认空时不作为排序字段，只有手动设置 ASC 和 DESC 才有效
   */
  public String orderBy() {
    return orderBy;
  }

  /**
   * 可查询
   */
  public boolean selectable() {
    return selectable;
  }

  /**
   * 可插入
   */
  public boolean insertable() {
    return insertable;
  }

  /**
   * 可更新
   */
  public boolean updatable() {
    return updatable;
  }

  /**
   * 排序方式
   */
  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  /**
   * 可查询
   */
  public void setSelectable(boolean selectable) {
    this.selectable = selectable;
  }

  /**
   * 可插入
   */
  public void setInsertable(boolean insertable) {
    this.insertable = insertable;
  }

  /**
   * 可更新
   */
  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  /**
   * 数据库类型 {, jdbcType=VARCHAR}
   */
  public void setJdbcType(JdbcType jdbcType) {
    this.jdbcType = jdbcType;
  }

  public Class<? extends TypeHandler> getTypeHandler() {
    return typeHandler;
  }

  /**
   * 类型处理器 {, typeHandler=XXTypeHandler}
   */
  public void setTypeHandler(Class<? extends TypeHandler> typeHandler) {
    this.typeHandler = typeHandler;
  }

  public String getNumericScale() {
    return numericScale;
  }

  /**
   * 小数位数 {, numericScale=2}
   */
  public void setNumericScale(String numericScale) {
    this.numericScale = numericScale;
  }
}
