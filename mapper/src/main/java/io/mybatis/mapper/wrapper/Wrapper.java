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
package io.mybatis.mapper.wrapper;

import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static io.mybatis.provider.EntityTable.DELIMITER;

/**
 * 通用的 Wrapper 查询对象
 *
 * @author dyun
 */
public class Wrapper<T> {
  /**
   * 排序字段
   */
  private String            orderByClause;
  /**
   * 是否使用 distinct
   */
  private boolean           distinct;
  /**
   * 是否允许 更新实体类字段可为空
   */
  private boolean           updateNullable;
  /**
   * 指定查询列
   */
  private String            selectColumns;
  /**
   * 指定查询列，不带 column As Alias 别名
   */
  private String            simpleSelectColumns;
  /**
   * 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  private String            startSql;
  /**
   * 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  private String            endSql;
  /**
   * 多组条件通过 OR 连接
   */
  private List<Criteria<T>> oredCriteria;
  /**
   * 最后一个条件, 不参与多组
   */
  private Criteria<T>       lastCriteria;
  /**
   * 设置 update 时的 set 字段
   */
  private List<Criterion>   setValues;

  /**
   * 默认构造方法，不允许Wrapper查询条件为空，不能操作全库
   */
  public Wrapper() {
    oredCriteria = new ArrayList<>();
    setValues = new ArrayList<>();
  }

  /**
   * or 条件
   *
   * @param criteria 条件
   */
  public void or(Criteria<T> criteria) {
    oredCriteria.add(criteria);
  }

  /**
   * 创建一个 or条件片段（不追加到当前Wrapper）
   *
   * @return 条件
   */
  public OrCriteria<T> orPart() {
    return new OrCriteria<>();
  }

  public Criteria<T> createCriteriaInternal() {
    return new Criteria<>(this);
  }

  /**
   * 清除所有设置
   */
  public void clear() {
    oredCriteria.clear();
    setValues.clear();
    orderByClause = null;
    distinct = false;
    updateNullable = false;
    selectColumns = null;
    simpleSelectColumns = null;
    startSql = null;
    endSql = null;
    lastCriteria = null;
  }

  /**
   * 清除条件，可重用
   */
  public Criteria<T> clean() {
    this.clear();
    return this.where();
  }

  /**
   * 创建一组条件，第一次调用时添加到默认条件中
   */
  public Criteria<T> where() {
    Criteria<T> criteria = createCriteriaInternal();
    if (this.oredCriteria.size() == 0) {
      this.oredCriteria.add(criteria);
    }
    return criteria;
  }

  /**
   * 指定查询列
   *
   * @param selectColumns 查询列
   */
  public Wrapper<T> select(String selectColumns) {
    this.selectColumns = selectColumns;
    return this;
  }

  /**
   * 设置简单查询列，不能带别名
   *
   * @param simpleSelectColumns 简单查询列
   */
  public Wrapper<T> selectSimple(String simpleSelectColumns) {
    this.simpleSelectColumns = simpleSelectColumns;
    return this;
  }

  /**
   * 指定查询列，多次调用会覆盖，设置时会清除 {@link #exclude}
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final Wrapper<T> select(Fn<T, Object>... fns) {
    selectColumns = "";
    simpleSelectColumns = "";
    if (fns == null || fns.length == 0) {
      return this;
    }
    select(Arrays.stream(fns).map(Wrapper::toEntityColumn).collect(Collectors.toList()));
    return this;
  }

  /**
   * 指定查询列，多次调用会覆盖，设置时会清除 {@link #exclude}
   *
   * @param columns 查询列
   */
  private void select(List<EntityColumn> columns) {
    StringBuilder sb = new StringBuilder(columns.size() * 16);
    StringBuilder simple = new StringBuilder(columns.size() * 16);
    for (EntityColumn entityColumn : columns) {
      String column = entityColumn.column();
      String field = entityColumn.field().getName();
      if (sb.length() != 0) {
        sb.append(",");
      }
      if (simple.length() != 0) {
        simple.append(",");
      }
      //fix 如果有设置 autoResultMap 就不能有 AS
      if (column.equals(field) || entityColumn.entityTable().useResultMaps()) {
        sb.append(column);
        simple.append(column);
      } else {
        Matcher matcher = DELIMITER.matcher(column);
        //eg: mysql `order` == field order | sqlserver [order] == field order
        simple.append(column);
        if (matcher.find() && field.equals(matcher.group(1))) {
          sb.append(column);
        } else {
          sb.append(column).append(" AS ").append(field);
        }
      }
    }
    selectColumns = sb.toString();
    simpleSelectColumns = simple.toString();
  }

  /**
   * 排除指定的查询列，设置时会清除 {@link #selectColumns}
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final Wrapper<T> exclude(Fn<T, Object>... fns) {
    selectColumns = "";
    simpleSelectColumns = "";
    if (fns == null || fns.length == 0) {
      return this;
    }
    //获取对应的实体类
    EntityTable table = toEntityColumn(fns[0]).entityTable();
    //排除列
    Set<String> excludeColumnSet = Arrays.stream(fns).map(Wrapper::toColumn).collect(Collectors.toSet());
    //设置
    select(table.selectColumns().stream()
        .filter(c -> !excludeColumnSet.contains(c.column())).collect(Collectors.toList()));
    return this;
  }

  /**
   * 获取查询列
   *
   * @return 查询列
   */
  public String getSelectColumns() {
    return selectColumns;
  }

  /**
   * 获取查询列，不带 column As Alias 别名
   *
   * @return 查询列
   */
  public String getSimpleSelectColumns() {
    return simpleSelectColumns;
  }

  /**
   * 获取起始 SQL
   *
   * @return 起始 SQL
   */
  public String getStartSql() {
    return startSql;
  }

  /**
   * 设置起始 SQL
   *
   * @param startSql 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  public Wrapper<T> startSql(String startSql) {
    this.startSql = startSql;
    return this;
  }

  /**
   * 获取结尾 SQL
   *
   * @return 结尾 SQL
   */
  public String getEndSql() {
    return endSql;
  }

  /**
   * 设置结尾 SQL
   *
   * @param endSql 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  public Wrapper<T> endSql(String endSql) {
    this.endSql = endSql;
    return this;
  }

  /**
   * 获取排序列
   *
   * @return 排序列
   */
  public String getOrderByClause() {
    return orderByClause;
  }

  /**
   * 设置排序列
   *
   * @param orderByClause 排序列
   */
  public Wrapper<T> setOrderByClause(String orderByClause) {
    this.orderByClause = orderByClause;
    return this;
  }

  /**
   * 获取所有条件
   *
   * @return 条件
   */
  public List<Criteria<T>> getOredCriteria() {
    return oredCriteria;
  }

  /**
   * 获取最后一个条件
   *
   * @return 条件
   */
  public Criteria<T> getlastCriteria() {
    return lastCriteria;
  }

  /**
   * 是否使用 distince
   *
   * @return distince
   */
  public boolean isDistinct() {
    return distinct;
  }

  /**
   * 设置 distince
   *
   * @param distinct true启用，false不使用
   */
  public Wrapper<T> distinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  /**
   * 是否 更新实体类字段可为空
   *
   * @return distince
   */
  public boolean isUpdateNullable() {
    return updateNullable;
  }

  /**
   * 设置 更新实体类字段可为空
   *
   * @param updateNullable true启用，false不使用
   */
  public Wrapper<T> updateNullable(boolean updateNullable) {
    this.updateNullable = updateNullable;
    return this;
  }

  /**
   * 设置更新字段和值
   *
   * @param setSql "column = value"
   */
  public Wrapper<T> set(String setSql) {
    this.setValues.add(new Criterion(setSql));
    return this;
  }

  /**
   * 设置更新字段和值
   *
   * @param fn    字段
   * @param value 值
   */
  public Wrapper<T> set(Fn<T, Object> fn, Object value) {
    this.setValues.add(new Criterion(toColumn(fn), value));
    return this;
  }

  /**
   * 设置更新字段和值
   *
   * @param useSet 表达式条件, true 使用，false 不使用
   * @param setSql "column = value"
   */
  public Wrapper<T> set(boolean useSet, String setSql) {
    return useSet ? set(setSql) : this;
  }

  /**
   * 设置更新字段和值
   *
   * @param useSet 表达式条件, true 使用，false 不使用
   * @param fn     字段
   * @param value  值
   */
  public Wrapper<T> set(boolean useSet, Fn<T, Object> fn, Object value) {
    return useSet ? set(fn, value) : this;
  }

  /**
   * 设置更新字段和值
   *
   * @param useSet   表达式条件, true 使用，false 不使用
   * @param fn       字段
   * @param supplier 值构造函数
   */
  public Wrapper<T> set(boolean useSet, Fn<T, Object> fn, Supplier<Object> supplier) {
    return useSet ? set(fn, supplier.get()) : this;
  }

  /**
   * 获取 set 值
   */
  public List<Criterion> getSetValues() {
    return setValues;
  }

  /**
   * criteria抽象类
   *
   * @param <T> 实体类类型
   * @param <E> 实现类类型
   */
  public static abstract class GeneratedCriteria<T, E> {
    public List<Criterion> criteria;

    /**
     * 获取实现类
     */
    public abstract E getSelf();

    protected GeneratedCriteria() {
      super();
      this.criteria = new ArrayList<>();
    }

    public List<Criterion> getCriteria() {
      return criteria;
    }

    public boolean isValid() {
      return criteria.size() > 0;
    }

    private String column(Fn<T, Object> fn) {
      return Wrapper.toColumn(fn);
    }

    /**
     * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
     *
     * @param condition 任意条件，例如 "length(countryname)<5"
     */
    public E apply(String condition) {
      if (condition == null) {
        throw new RuntimeException("Value for condition cannot be null");
      }
      criteria.add(new Criterion(condition));
      return getSelf();
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     */
    public E apply(String condition, Object value) {
      if (value == null) {
        throw new RuntimeException("Value for " + condition + " cannot be null");
      }
      criteria.add(new Criterion(condition, value));
      return getSelf();
    }

    /**
     * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param condition    任意条件，例如 "length(countryname)<5"
     */
    public E apply(boolean useCondition, String condition) {
      return useCondition ? apply(condition) : getSelf();
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param condition    例如 "length(countryname)="
     * @param value        例如 5
     */
    public E apply(boolean useCondition, String condition, Object value) {
      return useCondition ? apply(condition, value) : getSelf();
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param condition    例如 "length(countryname)="
     * @param supplier     任意条件值的构造函数
     */
    public E apply(boolean useCondition, String condition, Supplier<Object> supplier) {
      return useCondition ? apply(condition, supplier.get()) : getSelf();
    }

    /**
     * 用于between语句， 字段 between value1 and value 2
     *
     * @param condition 表达式条件, true 使用，false 不使用
     * @param value1    值1
     * @param value2    值2
     */
    private E apply(String condition, Object value1, Object value2) {
      if (value1 == null || value2 == null) {
        throw new RuntimeException("Between values for " + condition + " cannot be null");
      }
      criteria.add(new Criterion(condition, value1, value2));
      return getSelf();
    }

    /**
     * 指定字段为 null
     *
     * @param fn 字段对应的 get 方法引用
     */
    public E isNull(Fn<T, Object> fn) {
      apply(column(fn) + " IS NULL");
      return getSelf();
    }

    /**
     * 指定字段为 null
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     */
    public E isNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? isNull(fn) : getSelf();
    }

    /**
     * 指定字段不为 null
     *
     * @param fn 字段对应的 get 方法引用
     */
    public E isNotNull(Fn<T, Object> fn) {
      apply((column(fn) + " IS NOT NULL"));
      return getSelf();
    }

    /**
     * 指定字段不为 null
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     */
    public E isNotNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? isNotNull(fn) : getSelf();
    }

    /**
     * 字段 = 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E eq(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " =", value);
      return getSelf();
    }

    /**
     * 字段 = 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E eq(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? eq(fn, value) : getSelf();
    }

    /**
     * 字段 = 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数
     */
    public E eq(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? eq(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 != 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E ne(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " <>", value);
      return getSelf();
    }

    /**
     * 字段 != 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E ne(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? ne(fn, value) : getSelf();
    }

    /**
     * 字段 != 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数
     */
    public E ne(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? ne(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 > 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E gt(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " >", value);
      return getSelf();
    }

    /**
     * 字段 > 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E gt(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? gt(fn, value) : getSelf();
    }

    /**
     * 字段 > 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数
     */
    public E gt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? gt(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 >= 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E ge(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " >=", value);
      return getSelf();
    }

    /**
     * 字段 >= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E ge(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? ge(fn, value) : getSelf();
    }

    /**
     * 字段 >= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数
     */
    public E ge(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? ge(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 < 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E lt(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " <", value);
      return getSelf();
    }

    /**
     * 字段 < 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E lt(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? lt(fn, value) : getSelf();
    }

    /**
     * 字段 < 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     */
    public E lt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? lt(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 <= 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public E le(Fn<T, Object> fn, Object value) {
      apply(column(fn) + " <=", value);
      return getSelf();
    }

    /**
     * 字段 <= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public E le(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? le(fn, value) : getSelf();
    }

    /**
     * 字段 <= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数
     */
    public E le(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
      return useCondition ? le(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 in (值集合)
     *
     * @param fn     字段对应的 get 方法引用
     * @param values 值集合
     */
    @SuppressWarnings("rawtypes")
    public E in(Fn<T, Object> fn, Iterable values) {
      apply(column(fn) + " IN", values);
      return getSelf();
    }

    /**
     * 字段 in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param values       值集合
     */
    @SuppressWarnings("rawtypes")
    public E in(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? in(fn, values) : getSelf();
    }

    /**
     * 字段 in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值集合构造函数
     */
    @SuppressWarnings("rawtypes")
    public E in(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable> supplier) {
      return useCondition ? in(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 not in (值集合)
     *
     * @param fn     字段对应的 get 方法引用
     * @param values 值集合
     */
    @SuppressWarnings("rawtypes")
    public E notIn(Fn<T, Object> fn, Iterable values) {
      apply(column(fn) + " NOT IN", values);
      return getSelf();
    }

    /**
     * 字段 not in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param values       值集合
     */
    @SuppressWarnings("rawtypes")
    public E notIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? notIn(fn, values) : getSelf();
    }

    /**
     * 字段 not in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值集合构造函数
     */
    @SuppressWarnings("rawtypes")
    public E notIn(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable> supplier) {
      return useCondition ? notIn(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 between value1 and value 2
     *
     * @param fn     字段对应的 get 方法引用
     * @param value1 值1
     * @param value2 值2
     */
    public E between(Fn<T, Object> fn, Object value1, Object value2) {
      apply(column(fn) + " BETWEEN", value1, value2);
      return getSelf();
    }

    /**
     * 字段 between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value1       值1
     * @param value2       值2
     */
    public E between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? between(fn, value1, value2) : getSelf();
    }

    /**
     * 字段 between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier1    值1构造函数
     * @param supplier2    值2构造函数
     */
    public E between(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier1, Supplier<Object> supplier2) {
      return useCondition ? between(fn, supplier1.get(), supplier2.get()) : getSelf();
    }

    /**
     * 字段 not between value1 and value 2
     *
     * @param fn     字段对应的 get 方法引用
     * @param value1 值1
     * @param value2 值2
     */
    public E notBetween(Fn<T, Object> fn, Object value1, Object value2) {
      apply(column(fn) + " NOT BETWEEN", value1, value2);
      return getSelf();
    }

    /**
     * 字段 not between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value1       值1
     * @param value2       值2
     */
    public E notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? notBetween(fn, value1, value2) : getSelf();
    }

    /**
     * 字段 not between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier1    值1构造函数
     * @param supplier2    值2构造函数
     */
    public E notBetween(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier1, Supplier<Object> supplier2) {
      return useCondition ? notBetween(fn, supplier1.get(), supplier2.get()) : getSelf();
    }

    /**
     * 字段 like %值%
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，两侧自动添加 %
     */
    public E like(Fn<T, Object> fn, String value) {
      apply(column(fn) + " LIKE", "%" + value + "%");
      return getSelf();
    }

    /**
     * 字段 like %值%
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，两侧自动添加 %
     */
    public E like(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? like(fn, value) : getSelf();
    }

    /**
     * 字段 like %值%
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数，两侧自动添加 %
     */
    public E like(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
      return useCondition ? like(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 not like 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，两侧自动添加 %
     */
    public E notLike(Fn<T, Object> fn, String value) {
      apply(column(fn) + " NOT LIKE", "%" + value + "%");
      return getSelf();
    }

    /**
     * 字段 not like 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，两侧自动添加 %
     */
    public E notLike(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? notLike(fn, value) : getSelf();
    }

    /**
     * 字段 not like 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数，两侧自动添加 %
     */
    public E notLike(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
      return useCondition ? notLike(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 like 值%，匹配 value 为前缀的值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，右侧自动添加 %
     */
    public E startsWith(Fn<T, Object> fn, String value) {
      apply(column(fn) + " LIKE", value + "%");
      return getSelf();
    }

    /**
     * 字段 like 值%，匹配 value 为前缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，右侧自动添加 %
     */
    public E startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? startsWith(fn, value) : getSelf();
    }

    /**
     * 字段 like 值%，匹配 value 为前缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数，右侧自动添加 %
     */
    public E startsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
      return useCondition ? startsWith(fn, supplier.get()) : getSelf();
    }

    /**
     * 字段 like %值，匹配 value 为后缀的值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，左侧自动添加 %
     */
    public E endsWith(Fn<T, Object> fn, String value) {
      apply(column(fn) + " LIKE", "%" + value);
      return getSelf();
    }

    /**
     * 字段 like %值，匹配 value 为后缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，左侧自动添加 %
     */
    public E endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? endsWith(fn, value) : getSelf();
    }

    /**
     * 字段 like %值，匹配 value 为后缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param supplier     值构造函数，左侧自动添加 %
     */
    public E endsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
      return useCondition ? endsWith(fn, supplier.get()) : getSelf();
    }
  }

  public static class Criteria<T> extends GeneratedCriteria<T, Criteria<T>> {
    private Wrapper<T> currentWrapper;

    public Criteria() {
      super();
    }

    public Criteria(Wrapper<T> wrapper) {
      super();
      this.currentWrapper = wrapper;
    }

    @Override
    public Criteria<T> getSelf() {
      return this;
    }

    /**
     * 增加 AND 条件, 条件内的如果有多组条件用 OR 连接
     */
    @SafeVarargs
    public final Criteria<T> and(OrCriteria<T> orCriteria1, OrCriteria<T> orCriteria2, OrCriteria<T>... orCriterias) {
      List<OrCriteria<T>> orCriteriaList = new ArrayList<>(orCriterias != null ? orCriterias.length + 2 : 2);
      orCriteriaList.add(orCriteria1);
      orCriteriaList.add(orCriteria2);
      if (orCriterias != null) {
        orCriteriaList.addAll(Arrays.asList(orCriterias));
      }
      return and(orCriteriaList);
    }

    /**
     * 增加 AND 条件, 条件内的如果有多组条件用 OR 连接
     */
    public Criteria<T> and(List<OrCriteria<T>> orCriteriaList) {
      super.criteria.add(new Criterion(null, orCriteriaList));
      return this;
    }

    /**
     * 增加 AND 条件, 条件内的如果有多组条件用 OR 连接
     *
     * @param orParts 条件块
     */
    @SafeVarargs
    public final Criteria<T> and(Function<OrCriteria<T>, OrCriteria<T>>... orParts) {
      if (orParts != null && orParts.length > 0) {
        and(Arrays.stream(orParts).map(orPart -> orPart.apply(new Wrapper<T>().orPart())).collect(Collectors.toList()));
      }
      return this;
    }

    /**
     * or 一组条件
     *
     * @return 条件
     */
    public Criteria<T> or() {
      Criteria<T> criteria = currentWrapper.createCriteriaInternal();
      currentWrapper.oredCriteria.add(criteria);
      return criteria;
    }

    /**
     * 最后一组条件
     * <p>
     * 重复调用将覆盖
     *
     * @return 条件
     */
    public Criteria<T> last() {
      Criteria<T> criteria = currentWrapper.createCriteriaInternal();
      currentWrapper.lastCriteria = criteria;
      return criteria;
    }

    /**
     * 通过方法引用方式设置排序字段
     *
     * @param fn    排序列的方法引用
     * @param order 排序方式
     * @return Wrapper
     */
    public Criteria<T> orderBy(Fn<T, Object> fn, Order order) {
      if (currentWrapper.orderByClause == null) {
        currentWrapper.orderByClause = "";
      } else {
        currentWrapper.orderByClause += ", ";
      }
      currentWrapper.orderByClause += toColumn(fn) + " " + order;
      return this;
    }

    /**
     * 用于一些非常规的排序 或 简单的字符串形式的排序 <br>
     * 本方法 和 Wrapper.setOrderByClause 方法的区别是 <strong>本方法不会覆盖已有的排序内容</strong> <br>
     * eg：  ORDER BY status = 5 DESC  即将 status = 5 的放在最前面<br>
     * 此时入参为：<pre><code>Wrapper.orderBy("status = 5 DESC")</code></pre>
     *
     * @param orderByCondition 字符串排序表达式
     * @return Wrapper
     */
    public Criteria<T> orderBy(String orderByCondition) {
      if (orderByCondition != null && orderByCondition.length() > 0) {
        if (currentWrapper.orderByClause == null) {
          currentWrapper.orderByClause = "";
        } else {
          currentWrapper.orderByClause += ", ";
        }
        currentWrapper.orderByClause += orderByCondition;
      }
      return this;
    }


    /**
     * 用于一些特殊的非常规的排序，排序字符串需要通过一些函数或者方法来构造出来<br>
     * eg：  ORDER BY FIELD(id,3,1,2) 即将 id 按照 3，1，2 的顺序排序<br>
     * 此时入参为：<pre><code>Wrapper.orderBy(()-> {
     * return Stream.of(3,1,2)
     *              .map(Objects::toString)
     *              .collect(Collectors.joining("," , "FIELD( id ," , ")"));
     * })</code></pre>
     *
     * @param orderByCondition 字符串排序表达式
     * @return Wrapper
     */
    public Criteria<T> orderBy(Supplier<String> orderByCondition) {
      return orderBy(orderByCondition.get());
    }

    /**
     * 支持使用字符串形式来设置 order by，用以支持一些特殊的排序方案
     *
     * @param useOrderBy       条件表达式，true使用，false不使用 字符串排序
     * @param orderByCondition 排序字符串构造方法，比如通过数组集合循环拼接等
     * @return Wrapper
     */
    public Criteria<T> orderBy(boolean useOrderBy, Supplier<String> orderByCondition) {
      return useOrderBy ? orderBy(orderByCondition) : this;
    }

    /**
     * 通过方法引用方式设置排序字段，升序排序
     *
     * @param fns 排序列的方法引用
     * @return Wrapper
     */
    @SafeVarargs
    public final Criteria<T> orderByAsc(Fn<T, Object>... fns) {
      if (fns != null && fns.length > 0) {
        for (Fn<T, Object> fn : fns) {
          orderBy(fn, Order.ASC);
        }
      }
      return this;
    }

    /**
     * 通过方法引用方式设置排序字段，降序排序
     *
     * @param fns 排序列的方法引用
     * @return Wrapper
     */
    @SafeVarargs
    public final Criteria<T> orderByDesc(Fn<T, Object>... fns) {
      if (fns != null && fns.length > 0) {
        for (Fn<T, Object> fn : fns) {
          orderBy(fn, Order.DESC);
        }
      }
      return this;
    }

    /**
     * 获取当前wrapper
     */
    public Wrapper<T> build() {
      return currentWrapper;
    }

    /**
     * 执行消费
     */
    public <R> R execute(Function<Wrapper<T>, R> method) {
      return method.apply(currentWrapper);
    }

    /**
     * 执行消费
     */
    public <U, R> R execute(U entity, BiFunction<U, Wrapper<T>, R> method) {
      return method.apply(entity, currentWrapper);
    }
  }

  public static class OrCriteria<T> extends GeneratedCriteria<T, OrCriteria<T>> {
    public OrCriteria() {
      super();
    }

    @Override
    public OrCriteria<T> getSelf() {
      return this;
    }
  }

  public static class Criterion {
    private final String condition;

    private Object value;

    private Object secondValue;

    private boolean noValue;

    private boolean singleValue;

    private boolean betweenValue;

    private boolean listValue;

    private boolean orValue;

    public Criterion(String condition) {
      super();
      this.condition = condition;
      this.noValue = true;
    }

    public Criterion(String condition, Object value) {
      this(condition, value, null);
    }

    public Criterion(String condition, Object value, String typeHandler) {
      super();
      this.condition = condition;
      this.value = value;
      if (value instanceof Collection<?>) {
        if (condition != null) {
          this.listValue = true;
        } else {
          this.orValue = true;
        }
      } else {
        this.singleValue = true;
      }
    }

    public Criterion(String condition, Object value, Object secondValue) {
      this(condition, value, secondValue, null);
    }

    public Criterion(String condition, Object value, Object secondValue, String typeHandler) {
      super();
      this.condition = condition;
      this.value = value;
      this.secondValue = secondValue;
      this.betweenValue = true;
    }

    public String getCondition() {
      return condition;
    }

    public Object getValue() {
      return value;
    }

    public Object getSecondValue() {
      return secondValue;
    }

    public boolean isNoValue() {
      return noValue;
    }

    public boolean isSingleValue() {
      return singleValue;
    }

    public boolean isBetweenValue() {
      return betweenValue;
    }

    public boolean isListValue() {
      return listValue;
    }

    public boolean isOrValue() {
      if (orValue && this.value instanceof Collection) {
        return ((Collection<?>) this.value)
            .stream()
            .filter(item -> item instanceof OrCriteria)
            .map(OrCriteria.class::cast)
            .anyMatch(GeneratedCriteria::isValid);
      }
      return false;
    }
  }

  /**
   * 排序方式
   */
  public enum Order {
    /**
     * 升序
     */
    ASC,
    /**
     * 降序
     */
    DESC
  }

  /**
   * 查询条件是否为空
   *
   * @return 查询条件是否为空
   */
  public boolean isEmpty() {
    if (oredCriteria.size() == 0 && lastCriteria == null) {
      return true;
    }
    boolean noCriteria = true;
    List<Criteria<T>> checkList = new ArrayList<>(oredCriteria);
    if (lastCriteria != null) {
      checkList.add(lastCriteria);
    }
    for (Criteria<T> criteria : checkList) {
      if (!criteria.getCriteria().isEmpty()) {
        noCriteria = false;
        break;
      }
    }
    return noCriteria;
  }

  /**
   * 是否包含更新字段
   *
   * @param column 字段名
   * @return 字段值
   */
  public Boolean setValuesContains(String column) {
    return this.setValues.stream().anyMatch(criterion -> {
      if (criterion.noValue) {
        String[] split = criterion.getCondition().split("=");
        return split[0].contains(column);
      } else {
        return criterion.getCondition().contains(column);
      }
    });
  }

  /**
   * 转换为字段对应的列信息：获取方法引用对应的列信息
   *
   * @return 方法引用对应的列信息
   */
  public static <T> String toColumn(Fn<T, Object> fn) {
    return toEntityColumn(fn).column();
  }

  /**
   * 转换为字段对应的列信息：获取方法引用对应的列信息
   *
   * @return 方法引用对应的列信息
   */
  public static <T> EntityColumn toEntityColumn(Fn<T, Object> fn) {
    return fn.toEntityColumn();
  }
}
