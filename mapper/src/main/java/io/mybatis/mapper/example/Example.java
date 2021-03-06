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

package io.mybatis.mapper.example;

import io.mybatis.mapper.fn.Fn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
   * 允许Example查询条件为空
   */
  protected boolean           allowCriteriaEmpty;

  /**
   * 默认构造方法，不允许Example查询条件为空，不能操作全库
   */
  public Example() {
    oredCriteria = new ArrayList<>();
  }

  /**
   * 是否允许 Example 查询条件为空，允许空时可能会操作全库
   *
   * @param allowCriteriaEmpty 是否允许Example查询条件为空，默认 false 不允许
   */
  public Example(boolean allowCriteriaEmpty) {
    this();
    this.allowCriteriaEmpty = allowCriteriaEmpty;
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
    orderByClause = null;
    distinct = false;
    selectColumns = null;
    startSql = null;
    endSql = null;
    allowCriteriaEmpty = false;
  }

  /**
   * 指定查询列
   *
   * @param fns 方法引用
   */
  public Example<T> selectColumns(Fn<T, Object>... fns) {
    if (selectColumns == null) {
      selectColumns = "";
    }
    for (Fn<T, Object> fn : fns) {
      String column = fn.toColumn();
      String field = fn.toField();
      if (selectColumns.length() != 0) {
        selectColumns += ",";
      }
      if (column.equals(field)) {
        selectColumns += column;
      } else {
        selectColumns += column + " AS " + field;
      }
    }
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
  public void setSelectColumns(String selectColumns) {
    this.selectColumns = selectColumns;
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
  public void setStartSql(String startSql) {
    this.startSql = startSql;
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
  public void setEndSql(String endSql) {
    this.endSql = endSql;
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
    }
    orderByClause += fn.toColumn() + " " + order;
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
  public void setOrderByClause(String orderByClause) {
    this.orderByClause = orderByClause;
  }

  /**
   * 是否允许查询条件为空
   *
   * @return 是否允许查询条件为空，默认 false 不允许
   */
  public boolean isAllowCriteriaEmpty() {
    return allowCriteriaEmpty;
  }

  /**
   * 设置是否允许查询条件为空
   *
   * @param allowCriteriaEmpty true允许空，一般用于查询，false不允许空，一般用于修改和删除
   */
  public void setAllowCriteriaEmpty(boolean allowCriteriaEmpty) {
    this.allowCriteriaEmpty = allowCriteriaEmpty;
  }

  /**
   * 设置是否允许查询条件为空
   *
   * @param allowCriteriaEmpty true允许空，一般用于查询，false不允许空，一般用于修改和删除
   */
  public void allowCriteriaEmpty(boolean allowCriteriaEmpty) {
    this.allowCriteriaEmpty = allowCriteriaEmpty;
  }

  /**
   * 获取所有条件，当前方法会校验所有查询条件，如果不存在查询条件就抛出异常。
   * <p>
   * 不允许通过 Example 相关方法操作全表！！！
   *
   * @return 条件
   */
  public List<Criteria<T>> getOredCriteria() {
    if (!allowCriteriaEmpty) {
      if (oredCriteria.size() == 0) {
        throw new IllegalArgumentException("Example Criteria cannot be empty");
      }
      boolean noCriteria = true;
      for (Criteria<T> criteria : oredCriteria) {
        if (!criteria.getCriteria().isEmpty()) {
          noCriteria = false;
          break;
        }
      }
      if (noCriteria) {
        throw new IllegalArgumentException("Example Criteria cannot be empty");
      }
    }
    return oredCriteria;
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
  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
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

    public Criteria<T> andIsNull(Fn<T, Object> fn) {
      addCriterion(column(fn) + " IS NULL");
      return (Criteria<T>) this;
    }

    public Criteria<T> andIsNotNull(Fn<T, Object> fn) {
      addCriterion(column(fn) + " IS NOT NULL");
      return (Criteria<T>) this;
    }

    public Criteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " =", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <>", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " >", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " >=", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLessThan(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
      addCriterion(column(fn) + " <=", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andIn(Fn<T, Object> fn, Iterable values) {
      addCriterion(column(fn) + " IN", values);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
      addCriterion(column(fn) + " NOT IN", values);
      return (Criteria<T>) this;
    }

    public Criteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
      addCriterion(column(fn) + " BETWEEN", value1, value2);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
      addCriterion(column(fn) + " NOT BETWEEN", value1, value2);
      return (Criteria<T>) this;
    }

    public Criteria<T> andLike(Fn<T, Object> fn, String value) {
      addCriterion(column(fn) + "  LIKE", value);
      return (Criteria<T>) this;
    }

    public Criteria<T> andNotLike(Fn<T, Object> fn, String value) {
      addCriterion(column(fn) + "  NOT LIKE", value);
      return (Criteria<T>) this;
    }

    /**
     * 手写条件
     *
     * @param condition 例如 "length(countryname)<5"
     * @return
     */
    public Criteria<T> andCondition(String condition) {
      addCriterion(condition);
      return (Criteria<T>) this;
    }

    /**
     * 手写左边条件，右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     * @return
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

  public static class Criterion {
    private final String condition;

    private Object value;

    private Object secondValue;

    private boolean noValue;

    private boolean singleValue;

    private boolean betweenValue;

    private boolean listValue;

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
        this.listValue = true;
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
  }
}