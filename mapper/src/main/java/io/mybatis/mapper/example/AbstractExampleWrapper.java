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

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 抽象类： 封装 Example 的查询条件，方便链式调用
 *
 * @param <T> 实体类类型
 * @param <E> 实现类类型
 * @author liuzh
 */
public abstract class AbstractExampleWrapper<T, E> {
  protected final Example<T>          example;
  protected       Example.Criteria<T> current;

  public AbstractExampleWrapper(Example<T> example) {
    this.example = example;
    this.current = example.createCriteria();
  }

  public abstract E getSelf();

  /**
   * or 一组条件
   *
   * @return 条件
   */
  public E or() {
    this.current = this.example.or();
    return getSelf();
  }

  /**
   * 获取查询条件
   */
  public Example<T> example() {
    return example;
  }

  /**
   * 清除条件，可重用
   */
  public E clear() {
    this.example.clear();
    this.current = example.createCriteria();
    return getSelf();
  }

  /**
   * 指定查询列
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final E select(Fn<T, Object>... fns) {
    this.example.selectColumns(fns);
    return getSelf();
  }

  /**
   * 排除指定查询列
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final E exclude(Fn<T, Object>... fns) {
    this.example.excludeColumns(fns);
    return getSelf();
  }

  /**
   * 设置起始 SQL
   *
   * @param startSql 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  public E startSql(String startSql) {
    this.example.setStartSql(startSql);
    return getSelf();
  }

  /**
   * 设置结尾 SQL
   *
   * @param endSql 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  public E endSql(String endSql) {
    this.example.setEndSql(endSql);
    return getSelf();
  }

  /**
   * 通过方法引用方式设置排序字段
   *
   * @param fn    排序列的方法引用
   * @param order 排序方式
   * @return Example
   */
  public E orderBy(Fn<T, Object> fn, Example.Order order) {
    this.example.orderBy(fn, order);
    return getSelf();
  }

  /**
   * 支持使用字符串形式来设置 order by，用以支持一些特殊的排序方案
   *
   * @param orderByCondition 排序字符串（不会覆盖已有的排序内容）
   * @return Example
   */
  public E orderBy(String orderByCondition) {
    this.example.orderBy(orderByCondition);
    return getSelf();
  }

  /**
   * 支持使用字符串形式来设置 order by，用以支持一些特殊的排序方案
   *
   * @param orderByCondition 排序字符串构造方法,比如通过数组集合循环拼接等
   * @return Example
   */
  public E orderBy(Supplier<String> orderByCondition) {
    this.example.orderBy(orderByCondition);
    return getSelf();
  }

  /**
   * 支持使用字符串形式来设置 order by，用以支持一些特殊的排序方案
   *
   * @param useOrderBy       条件表达式，true使用，false不使用 字符串排序
   * @param orderByCondition 排序字符串构造方法，比如通过数组集合循环拼接等
   * @return Example
   */
  public E orderBy(boolean useOrderBy, Supplier<String> orderByCondition) {
    return useOrderBy ? this.orderBy(orderByCondition) : getSelf();
  }

  /**
   * 通过方法引用方式设置排序字段，升序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final E orderByAsc(Fn<T, Object>... fns) {
    this.example.orderByAsc(fns);
    return getSelf();
  }

  /**
   * 通过方法引用方式设置排序字段，降序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final E orderByDesc(Fn<T, Object>... fns) {
    this.example.orderByDesc(fns);
    return getSelf();
  }

  /**
   * 设置 distince
   */
  public E distinct() {
    this.example.setDistinct(true);
    return getSelf();
  }

  /**
   * 设置更新字段和值
   *
   * @param useSet 表达式条件, true 使用，false 不使用
   * @param setSql "column = value"
   */
  public E set(boolean useSet, String setSql) {
    return useSet ? set(setSql) : getSelf();
  }

  /**
   * 设置更新字段和值
   *
   * @param setSql "column = value"
   */
  public E set(String setSql) {
    this.example.set(setSql);
    return getSelf();
  }

  /**
   * 设置更新字段和值
   *
   * @param useSet 表达式条件, true 使用，false 不使用
   * @param fn     字段
   * @param value  值
   */
  public E set(boolean useSet, Fn<T, Object> fn, Object value) {
    return useSet ? set(fn, value) : getSelf();
  }


  /**
   * 设置更新字段和值
   *
   * @param useSet   表达式条件, true 使用，false 不使用
   * @param fn       字段
   * @param supplier 值构造函数
   */
  public E set(boolean useSet, Fn<T, Object> fn, Supplier<Object> supplier) {
    return useSet ? set(fn, supplier.get()) : getSelf();
  }


  /**
   * 设置更新字段和值
   *
   * @param fn    字段
   * @param value 值
   */
  public E set(Fn<T, Object> fn, Object value) {
    this.example.set(fn, value);
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
   * 指定字段为 null
   *
   * @param fn 字段对应的 get 方法引用
   */
  public E isNull(Fn<T, Object> fn) {
    this.current.addCriterion(fn.toColumn() + " IS NULL");
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
   * 指定字段不为 null
   *
   * @param fn 字段对应的 get 方法引用
   */
  public E isNotNull(Fn<T, Object> fn) {
    this.current.addCriterion(fn.toColumn() + " IS NOT NULL");
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
   * 字段 = 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E eq(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " =", value);
    return getSelf();
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
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E ne(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <>", value);
    return getSelf();
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
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E gt(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " >", value);
    return getSelf();
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
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E ge(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " >=", value);
    return getSelf();
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
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E lt(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <", value);
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
   * 字段 <= 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public E le(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <=", value);
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
   * 字段 in (值集合)
   *
   * @param fn     字段对应的 get 方法引用
   * @param values 值集合
   */
  @SuppressWarnings("rawtypes")
  public E in(Fn<T, Object> fn, Iterable values) {
    this.current.addCriterion(fn.toColumn() + " IN", values);
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
   * 字段 not in (值集合)
   *
   * @param fn     字段对应的 get 方法引用
   * @param values 值集合
   */
  @SuppressWarnings("rawtypes")
  public E notIn(Fn<T, Object> fn, Iterable values) {
    this.current.addCriterion(fn.toColumn() + " NOT IN", values);
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
   * 字段 between value1 and value 2
   *
   * @param fn     字段对应的 get 方法引用
   * @param value1 值1
   * @param value2 值2
   */
  public E between(Fn<T, Object> fn, Object value1, Object value2) {
    this.current.addCriterion(fn.toColumn() + " BETWEEN", value1, value2);
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
   * 字段 not between value1 and value 2
   *
   * @param fn     字段对应的 get 方法引用
   * @param value1 值1
   * @param value2 值2
   */
  public E notBetween(Fn<T, Object> fn, Object value1, Object value2) {
    this.current.addCriterion(fn.toColumn() + " NOT BETWEEN", value1, value2);
    return getSelf();
  }

  /**
   * 字段 like %值%
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param value        值，两侧自动添加 %
   */
  public E contains(boolean useCondition, Fn<T, Object> fn, String value) {
    return useCondition ? contains(fn, value) : getSelf();
  }

  /**
   * 字段 like %值%
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param supplier     值构造函数，两侧自动添加 %
   */
  public E contains(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
    return useCondition ? contains(fn, supplier.get()) : getSelf();
  }

  /**
   * 字段 like %值%
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，两侧自动添加 %
   */
  public E contains(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", "%" + value + "%");
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
   * 字段 like 值%，匹配 value 为前缀的值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，右侧自动添加 %
   */
  public E startsWith(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", value + "%");
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

  /**
   * 字段 like %值，匹配 value 为后缀的值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，左侧自动添加 %
   */
  public E endsWith(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", "%" + value);
    return getSelf();
  }

  /**
   * 字段 like 值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param value        值，需要指定 '%'(多个), '_'(单个) 模糊匹配
   */
  public E like(boolean useCondition, Fn<T, Object> fn, String value) {
    return useCondition ? like(fn, value) : getSelf();
  }

  /**
   * 字段 like 值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param supplier     值构造函数，需要指定 '%'(多个), '_'(单个) 模糊匹配
   */
  public E like(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
    return useCondition ? like(fn, supplier.get()) : getSelf();
  }

  /**
   * 字段 like 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，需要指定 '%'(多个), '_'(单个) 模糊匹配
   */
  public E like(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", value);
    return getSelf();
  }

  /**
   * 字段 not like 值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param value        值，需要指定 % 模糊匹配
   */
  public E notLike(boolean useCondition, Fn<T, Object> fn, String value) {
    return useCondition ? notLike(fn, value) : getSelf();
  }

  /**
   * 字段 not like 值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param fn           字段对应的 get 方法引用
   * @param supplier     值构造函数，需要指定 % 模糊匹配
   */
  public E notLike(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
    return useCondition ? notLike(fn, supplier.get()) : getSelf();
  }

  /**
   * 字段 not like 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，需要指定 % 模糊匹配
   */
  public E notLike(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  NOT LIKE", value);
    return getSelf();
  }

  /**
   * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param condition    任意条件，例如 "length(countryname)<5"
   */
  public E anyCondition(boolean useCondition, String condition) {
    return useCondition ? anyCondition(condition) : getSelf();
  }

  /**
   * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
   *
   * @param condition 任意条件，例如 "length(countryname)<5"
   */
  public E anyCondition(String condition) {
    this.current.andCondition(condition);
    return getSelf();
  }

  /**
   * 手写左边条件，右边用value值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param condition    例如 "length(countryname)="
   * @param value        例如 5
   */
  public E anyCondition(boolean useCondition, String condition, Object value) {
    return useCondition ? anyCondition(condition, value) : getSelf();
  }

  /**
   * 手写左边条件，右边用value值
   *
   * @param useCondition 表达式条件, true 使用，false 不使用
   * @param condition    例如 "length(countryname)="
   * @param supplier     任意条件值的构造函数
   */
  public E anyCondition(boolean useCondition, String condition, Supplier<Object> supplier) {
    return useCondition ? anyCondition(condition, supplier.get()) : getSelf();
  }

  /**
   * 手写左边条件，右边用value值
   *
   * @param condition 例如 "length(countryname)="
   * @param value     例如 5
   */
  public E anyCondition(String condition, Object value) {
    this.current.andCondition(condition, value);
    return getSelf();
  }

  /**
   * 嵌套 or 查询，数组多个条件直接使用 or，单个条件中为 and
   *
   * @param orParts 条件块
   */
  @SafeVarargs
  public final E or(Function<Example.OrCriteria<T>, Example.OrCriteria<T>>... orParts) {
    if (orParts != null && orParts.length > 0) {
      this.current.andOr(Arrays.stream(orParts).map(orPart -> orPart.apply(example.orPart())).collect(Collectors.toList()));
    }
    return getSelf();
  }

}
