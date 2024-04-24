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

import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static io.mybatis.provider.EntityTable.DELIMITER;

/**
 * 通用的 Example 查询对象
 *
 * @author liuzh
 */
public class Example<T> {
  /**
   * 排序字段
   */
  protected String            orderByClause;
  /**
   * 是否使用 distinct
   */
  protected boolean           distinct;
  /**
   * 指定查询列
   */
  protected String            selectColumns;
  /**
   * 指定查询列，不带 column As Alias 别名
   */
  protected String            simpleSelectColumns;
  /**
   * 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  protected String            startSql;
  /**
   * 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  protected String            endSql;
  /**
   * 多组条件通过 OR 连接
   */
  protected List<Criteria<T>> oredCriteria;
  /**
   * 设置 update 时的 set 字段
   */
  protected List<Criterion>   setValues;

  /**
   * 默认构造方法，不允许Example查询条件为空，不能操作全库
   */
  public Example() {
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
   * or 一组条件
   *
   * @return 条件
   */
  public Criteria<T> or() {
    Criteria<T> criteria = createCriteriaInternal();
    oredCriteria.add(criteria);
    return criteria;
  }

  /**
   * 创建一个 or条件片段（不追加到当前Example）
   *
   * @return 条件
   */
  public OrCriteria<T> orPart() {
    return new OrCriteria<>();
  }

  /**
   * 创建一组条件，第一次调用时添加到默认条件中
   *
   * @return 条件
   */
  public Criteria<T> createCriteria() {
    Criteria<T> criteria = createCriteriaInternal();
    if (oredCriteria.size() == 0) {
      oredCriteria.add(criteria);
    }
    return criteria;
  }

  protected Criteria<T> createCriteriaInternal() {
    return new Criteria<>();
  }

  /**
   * 清除所有设置
   */
  public void clear() {
    oredCriteria.clear();
    setValues.clear();
    orderByClause = null;
    distinct = false;
    selectColumns = null;
    simpleSelectColumns = null;
    startSql = null;
    endSql = null;
  }

  /**
   * 指定查询列，多次调用会覆盖，设置时会清除 {@link #excludeColumns}
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final Example<T> selectColumns(Fn<T, Object>... fns) {
    selectColumns = "";
    simpleSelectColumns = "";
    if (fns == null || fns.length == 0) {
      return this;
    }
    selectColumns(Arrays.stream(fns).map(Fn::toEntityColumn).collect(Collectors.toList()));
    return this;
  }

  /**
   * 指定查询列，多次调用会覆盖，设置时会清除 {@link #excludeColumns}
   *
   * @param columns 查询列
   */
  private void selectColumns(List<EntityColumn> columns) {
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
  public final Example<T> excludeColumns(Fn<T, Object>... fns) {
    selectColumns = "";
    simpleSelectColumns = "";
    if (fns == null || fns.length == 0) {
      return this;
    }
    //获取对应的实体类
    EntityTable table = fns[0].toEntityColumn().entityTable();
    //排除列
    Set<String> excludeColumnSet = Arrays.stream(fns).map(Fn::toColumn).collect(Collectors.toSet());
    //设置
    selectColumns(table.selectColumns().stream()
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
   * 指定查询列
   *
   * @param selectColumns 查询列
   */
  public Example<T> setSelectColumns(String selectColumns) {
    this.selectColumns = selectColumns;
    return this;
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
   * 设置简单查询列，不能带别名
   *
   * @param simpleSelectColumns 简单查询列
   */
  public Example<T> setSimpleSelectColumns(String simpleSelectColumns) {
    this.simpleSelectColumns = simpleSelectColumns;
    return this;
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
  public Example<T> setStartSql(String startSql) {
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
  public Example<T> setEndSql(String endSql) {
    this.endSql = endSql;
    return this;
  }

  /**
   * 通过方法引用方式设置排序字段
   *
   * @param fn    排序列的方法引用
   * @param order 排序方式
   * @return Example
   */
  public Example<T> orderBy(Fn<T, Object> fn, Order order) {
    if (orderByClause == null) {
      orderByClause = "";
    } else {
      orderByClause += ", ";
    }
    orderByClause += fn.toColumn() + " " + order;
    return this;
  }

  /**
   * 用于一些非常规的排序 或 简单的字符串形式的排序 <br>
   * 本方法 和 example.setOrderByClause 方法的区别是 <strong>本方法不会覆盖已有的排序内容</strong> <br>
   * eg：  ORDER BY status = 5 DESC  即将 status = 5 的放在最前面<br>
   * 此时入参为：<pre><code>example.orderBy("status = 5 DESC")</code></pre>
   *
   * @param orderByCondition 字符串排序表达式
   * @return Example
   */
  public Example<T> orderBy(String orderByCondition) {
    if (orderByCondition != null && orderByCondition.length() > 0) {
      if (orderByClause == null) {
        orderByClause = "";
      } else {
        orderByClause += ", ";
      }
      orderByClause += orderByCondition;
    }
    return this;
  }


  /**
   * 用于一些特殊的非常规的排序，排序字符串需要通过一些函数或者方法来构造出来<br>
   * eg：  ORDER BY FIELD(id,3,1,2) 即将 id 按照 3，1，2 的顺序排序<br>
   * 此时入参为：<pre><code>example.orderBy(()-> {
   * return Stream.of(3,1,2)
   *              .map(Objects::toString)
   *              .collect(Collectors.joining("," , "FIELD( id ," , ")"));
   * })</code></pre>
   *
   * @param orderByCondition 字符串排序表达式
   * @return Example
   */
  public Example<T> orderBy(Supplier<String> orderByCondition) {
    return orderBy(orderByCondition.get());
  }

  /**
   * 通过方法引用方式设置排序字段，升序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final Example<T> orderByAsc(Fn<T, Object>... fns) {
    if (fns != null && fns.length > 0) {
      for (int i = 0; i < fns.length; i++) {
        orderBy(fns[i], Order.ASC);
      }
    }
    return this;
  }

  /**
   * 通过方法引用方式设置排序字段，降序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final Example<T> orderByDesc(Fn<T, Object>... fns) {
    if (fns != null && fns.length > 0) {
      for (int i = 0; i < fns.length; i++) {
        orderBy(fns[i], Order.DESC);
      }
    }
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
  public Example<T> setOrderByClause(String orderByClause) {
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
   * 获取 set 值
   */
  public List<Criterion> getSetValues() {
    return setValues;
  }

  /**
   * 查询条件是否为空
   *
   * @return
   */
  public boolean isEmpty() {
    if (oredCriteria.size() == 0) {
      return true;
    }
    boolean noCriteria = true;
    for (Criteria<T> criteria : oredCriteria) {
      if (!criteria.getCriteria().isEmpty()) {
        noCriteria = false;
        break;
      }
    }
    return noCriteria;
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
  public Example<T> setDistinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  /**
   * 设置更新字段和值
   *
   * @param setSql "column = value"
   */
  public Example<T> set(String setSql) {
    this.setValues.add(new Criterion(setSql));
    return this;
  }

  /**
   * 设置更新字段和值
   *
   * @param fn    字段
   * @param value 值
   */
  public Example<T> set(Fn<T, Object> fn, Object value) {
    this.setValues.add(new Criterion(fn.toColumn(), value));
    return this;
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

  protected static abstract class GeneratedCriteria<T> {
    protected List<Criterion> criteria;

    protected GeneratedCriteria() {
      super();
      this.criteria = new ArrayList<>();
    }

    private String column(Fn<T, Object> fn) {
      return fn.toColumn();
    }

    protected void addCriterion(String condition) {
      if (condition == null) {
        throw new RuntimeException("Value for condition cannot be null");
      }
      criteria.add(new Criterion(condition));
    }

    protected void addCriterion(String condition, Object value) {
      if (value == null) {
        throw new RuntimeException("Value for " + condition + " cannot be null");
      }
      criteria.add(new Criterion(condition, value));
    }

    protected void addCriterion(String condition, Object value1, Object value2) {
      if (value1 == null || value2 == null) {
        throw new RuntimeException("Between values for " + condition + " cannot be null");
      }
      criteria.add(new Criterion(condition, value1, value2));
    }

    public Criteria<T> andIsNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? andIsNull(fn) : (Criteria<T>) this;
    }

    public Criteria<T> andIsNull(Fn<T, Object> fn) {
      addCriterion(column(fn) + " IS NULL");
      return (Criteria<T>) this;
    }

    public Criteria<T> andIsNotNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? andIsNotNull(fn) : (Criteria<T>) this;
    }

    public Criteria<T> andIsNotNull(Fn<T, Object> fn) {
      addCriterion(column(fn) + " IS NOT NULL");
      return (Criteria<T>) this;
    }

    public Criteria<T> andEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andEqualTo(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " =", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andNotEqualTo(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <>", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThan(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andGreaterThan(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " >", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andGreaterThanOrEqualTo(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " >=", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLessThan(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andLessThan(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andLessThan(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLessThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? andLessThanOrEqualTo(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <=", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? andIn(fn, values) : (Criteria<T>) this;
    }

    @SuppressWarnings("rawtypes")
    public Criteria<T> andIn(Fn<T, Object> fn, Iterable values) {
      addCriterion(column(fn) + " IN", values);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? andNotIn(fn, values) : (Criteria<T>) this;
    }

    @SuppressWarnings("rawtypes")
    public Criteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
      addCriterion(column(fn) + " NOT IN", values);
      return (Criteria<T>) this;
    }

    public Criteria<T> andBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? andBetween(fn, value1, value2) : (Criteria<T>) this;
    }

    public Criteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
      addCriterion(column(fn) + " BETWEEN", value1, value2);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? andNotBetween(fn, value1, value2) : (Criteria<T>) this;
    }

    public Criteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
      addCriterion(column(fn) + " NOT BETWEEN", value1, value2);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLike(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? andLike(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andLike(Fn<T, Object> fn, String value) {
      addCriterion(column(fn) + "  LIKE", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotLike(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? andNotLike(fn, value) : (Criteria<T>) this;
    }

    public Criteria<T> andNotLike(Fn<T, Object> fn, String value) {
      addCriterion(column(fn) + "  NOT LIKE", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andOr(OrCriteria<T> orCriteria1, OrCriteria<T> orCriteria2, OrCriteria<T>... orCriterias) {
      List<OrCriteria<T>> orCriteriaList = new ArrayList<>(orCriterias != null ? orCriterias.length + 2 : 2);
      orCriteriaList.add(orCriteria1);
      orCriteriaList.add(orCriteria2);
      if (orCriterias != null) {
        orCriteriaList.addAll(Arrays.asList(orCriterias));
      }
      return andOr(orCriteriaList);
    }

    public Criteria<T> andOr(List<OrCriteria<T>> orCriteriaList) {
      criteria.add(new Criterion(null, orCriteriaList));
      return (Criteria<T>) this;
    }

    public Criteria<T> andCondition(boolean useCondition, String condition) {
      return useCondition ? andCondition(condition) : (Criteria<T>) this;
    }

    /**
     * 手写条件
     *
     * @param condition 例如 "length(countryname)<5"
     */
    public Criteria<T> andCondition(String condition) {
      addCriterion(condition);
      return (Criteria<T>) this;
    }

    public Criteria<T> andCondition(boolean useCondition, String condition, Object value) {
      return useCondition ? andCondition(condition, value) : (Criteria<T>) this;
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     */
    public Criteria<T> andCondition(String condition, Object value) {
      criteria.add(new Criterion(condition, value));
      return (Criteria<T>) this;
    }


    public List<Criterion> getCriteria() {
      return criteria;
    }

    public boolean isValid() {
      return criteria.size() > 0;
    }
  }

  public static class Criteria<T> extends GeneratedCriteria<T> {

    protected Criteria() {
      super();
    }
  }

  public static class OrCriteria<T> extends Criteria<T> {

    protected OrCriteria() {
      super();
    }

    @Override
    @Deprecated
    public final OrCriteria<T> andOr(OrCriteria<T> orCriteria1, OrCriteria<T> orCriteria2, OrCriteria<T>... orCriterias) {
      throw new UnsupportedOperationException("Currently does not support nested [OR] operations.");
    }

    @Override
    @Deprecated
    public final OrCriteria<T> andOr(List<OrCriteria<T>> orCriteriaList) {
      throw new UnsupportedOperationException("Currently does not support nested [OR] operations.");
    }

    @Override
    public OrCriteria<T> andIsNull(Fn<T, Object> fn) {
      super.andIsNull(fn);
      return this;
    }

    @Override
    public OrCriteria<T> andIsNotNull(Fn<T, Object> fn) {
      super.andIsNotNull(fn);
      return this;
    }

    @Override
    public OrCriteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
      super.andEqualTo(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
      super.andNotEqualTo(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
      super.andGreaterThan(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
      super.andGreaterThanOrEqualTo(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andLessThan(Fn<T, Object> fn, Object value) {
      super.andLessThan(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
      super.andLessThanOrEqualTo(fn, value);
      return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> andIn(Fn<T, Object> fn, Iterable values) {
      super.andIn(fn, values);
      return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
      super.andNotIn(fn, values);
      return this;
    }

    @Override
    public OrCriteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
      super.andBetween(fn, value1, value2);
      return this;
    }

    @Override
    public OrCriteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
      super.andNotBetween(fn, value1, value2);
      return this;
    }

    @Override
    public OrCriteria<T> andLike(Fn<T, Object> fn, String value) {
      super.andLike(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andNotLike(Fn<T, Object> fn, String value) {
      super.andNotLike(fn, value);
      return this;
    }

    @Override
    public OrCriteria<T> andCondition(String condition) {
      super.andCondition(condition);
      return this;
    }

    @Override
    public OrCriteria<T> andCondition(String condition, Object value) {
      super.andCondition(condition, value);
      return this;
    }

    /**
     * 指定字段为 null
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     */
    public OrCriteria<T> isNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? isNull(fn) : this;
    }

    /**
     * 指定字段为 null
     *
     * @param fn 字段对应的 get 方法引用
     */
    public OrCriteria<T> isNull(Fn<T, Object> fn) {
      super.andIsNull(fn);
      return this;
    }

    /**
     * 指定字段不为 null
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     */
    public OrCriteria<T> isNotNull(boolean useCondition, Fn<T, Object> fn) {
      return useCondition ? isNotNull(fn) : this;
    }

    /**
     * 指定字段不为 null
     *
     * @param fn 字段对应的 get 方法引用
     */
    public OrCriteria<T> isNotNull(Fn<T, Object> fn) {
      super.andIsNotNull(fn);
      return this;
    }

    /**
     * 字段 = 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> eq(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? eq(fn, value) : this;
    }

    /**
     * 字段 = 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> eq(Fn<T, Object> fn, Object value) {
      super.andEqualTo(fn, value);
      return this;
    }

    /**
     * 字段 != 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> ne(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? ne(fn, value) : this;
    }

    /**
     * 字段 != 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> ne(Fn<T, Object> fn, Object value) {
      super.andNotEqualTo(fn, value);
      return this;
    }

    /**
     * 字段 > 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> gt(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? gt(fn, value) : this;
    }


    /**
     * 字段 > 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> gt(Fn<T, Object> fn, Object value) {
      super.andGreaterThan(fn, value);
      return this;
    }

    /**
     * 字段 >= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> ge(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? ge(fn, value) : this;
    }

    /**
     * 字段 >= 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> ge(Fn<T, Object> fn, Object value) {
      super.andGreaterThanOrEqualTo(fn, value);
      return this;
    }

    /**
     * 字段 < 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> lt(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? lt(fn, value) : this;
    }


    /**
     * 字段 < 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> lt(Fn<T, Object> fn, Object value) {
      super.andLessThan(fn, value);
      return this;
    }

    /**
     * 字段 <= 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值
     */
    public OrCriteria<T> le(boolean useCondition, Fn<T, Object> fn, Object value) {
      return useCondition ? le(fn, value) : this;
    }

    /**
     * 字段 <= 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值
     */
    public OrCriteria<T> le(Fn<T, Object> fn, Object value) {
      super.andLessThanOrEqualTo(fn, value);
      return this;
    }

    /**
     * 字段 in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param values       值集合
     */
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> in(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? in(fn, values) : this;
    }

    /**
     * 字段 in (值集合)
     *
     * @param fn     字段对应的 get 方法引用
     * @param values 值集合
     */
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> in(Fn<T, Object> fn, Iterable values) {
      super.andIn(fn, values);
      return this;
    }

    /**
     * 字段 not in (值集合)
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param values       值集合
     */
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> notIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
      return useCondition ? notIn(fn, values) : this;
    }

    /**
     * 字段 not in (值集合)
     *
     * @param fn     字段对应的 get 方法引用
     * @param values 值集合
     */
    @SuppressWarnings("rawtypes")
    public OrCriteria<T> notIn(Fn<T, Object> fn, Iterable values) {
      super.andNotIn(fn, values);
      return this;
    }

    /**
     * 字段 between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value1       值1
     * @param value2       值2
     */
    public OrCriteria<T> between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? between(fn, value1, value2) : this;
    }

    /**
     * 字段 between value1 and value 2
     *
     * @param fn     字段对应的 get 方法引用
     * @param value1 值1
     * @param value2 值2
     */
    public OrCriteria<T> between(Fn<T, Object> fn, Object value1, Object value2) {
      super.andBetween(fn, value1, value2);
      return this;
    }

    /**
     * 字段 not between value1 and value 2
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value1       值1
     * @param value2       值2
     */
    public OrCriteria<T> notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
      return useCondition ? notBetween(fn, value1, value2) : this;
    }


    /**
     * 字段 not between value1 and value 2
     *
     * @param fn     字段对应的 get 方法引用
     * @param value1 值1
     * @param value2 值2
     */
    public OrCriteria<T> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
      super.andNotBetween(fn, value1, value2);
      return this;
    }

    /**
     * 字段 like %值%
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，两侧自动添加 %
     */
    public OrCriteria<T> contains(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? contains(fn, value) : this;
    }

    /**
     * 字段 like %值%
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，两侧自动添加 %
     */
    public OrCriteria<T> contains(Fn<T, Object> fn, String value) {
      super.andLike(fn, "%" + value + "%");
      return this;
    }

    /**
     * 字段 like 值%，匹配 value 为前缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，右侧自动添加 %
     */
    public OrCriteria<T> startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? startsWith(fn, value) : this;
    }

    /**
     * 字段 like 值%，匹配 value 为前缀的值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，右侧自动添加 %
     */
    public OrCriteria<T> startsWith(Fn<T, Object> fn, String value) {
      super.andLike(fn, value + "%");
      return this;
    }

    /**
     * 字段 like %值，匹配 value 为后缀的值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，左侧自动添加 %
     */
    public OrCriteria<T> endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? endsWith(fn, value) : this;
    }

    /**
     * 字段 like %值，匹配 value 为后缀的值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，左侧自动添加 %
     */
    public OrCriteria<T> endsWith(Fn<T, Object> fn, String value) {
      super.andLike(fn, "%" + value);
      return this;
    }

    /**
     * 字段 like 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，需要指定 '%'(多个), '_'(单个) 模糊匹配
     */
    public OrCriteria<T> like(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? like(fn, value) : this;
    }

    /**
     * 字段 like 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，需要指定 '%'(多个), '_'(单个) 模糊匹配
     */
    public OrCriteria<T> like(Fn<T, Object> fn, String value) {
      super.andLike(fn, value);
      return this;
    }

    /**
     * 字段 not like 值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param fn           字段对应的 get 方法引用
     * @param value        值，需要指定 % 模糊匹配
     */
    public OrCriteria<T> notLike(boolean useCondition, Fn<T, Object> fn, String value) {
      return useCondition ? notLike(fn, value) : this;
    }

    /**
     * 字段 not like 值
     *
     * @param fn    字段对应的 get 方法引用
     * @param value 值，需要指定 % 模糊匹配
     */
    public OrCriteria<T> notLike(Fn<T, Object> fn, String value) {
      super.andNotLike(fn, value);
      return this;
    }

    /**
     * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param condition    任意条件，例如 "length(countryname)<5"
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition) {
      return useCondition ? anyCondition(condition) : this;
    }

    /**
     * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
     *
     * @param condition 任意条件，例如 "length(countryname)<5"
     */
    public OrCriteria<T> anyCondition(String condition) {
      super.andCondition(condition);
      return this;
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param useCondition 表达式条件, true 使用，false 不使用
     * @param condition    例如 "length(countryname)="
     * @param value        例如 5
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition, Object value) {
      return useCondition ? anyCondition(condition, value) : this;
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     */
    public OrCriteria<T> anyCondition(String condition, Object value) {
      super.andCondition(condition, value);
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

    protected Criterion(String condition, Object value) {
      this(condition, value, null);
    }

    protected Criterion(String condition) {
      super();
      this.condition = condition;
      this.noValue = true;
    }

    protected Criterion(String condition, Object value, String typeHandler) {
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

    protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
      super();
      this.condition = condition;
      this.value = value;
      this.secondValue = secondValue;
      this.betweenValue = true;
    }

    protected Criterion(String condition, Object value, Object secondValue) {
      this(condition, value, secondValue, null);
    }

    public String getCondition() {
      return condition;
    }

    public Object getSecondValue() {
      return secondValue;
    }

    public Object getValue() {
      return value;
    }

    public boolean isBetweenValue() {
      return betweenValue;
    }

    public boolean isListValue() {
      return listValue;
    }

    public boolean isNoValue() {
      return noValue;
    }

    public boolean isSingleValue() {
      return singleValue;
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
}
