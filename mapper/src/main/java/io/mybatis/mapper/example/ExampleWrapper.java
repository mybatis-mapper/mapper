package io.mybatis.mapper.example;

import io.mybatis.mapper.BaseMapper;
import io.mybatis.mapper.fn.Fn;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 封装 Example 的查询条件，方便链式调用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public class ExampleWrapper<T, I extends Serializable> {
  private final BaseMapper<T, I>    baseMapper;
  private final Example<T>          example;
  private       Example.Criteria<T> current;

  public ExampleWrapper(BaseMapper<T, I> baseMapper, Example<T> example) {
    this.baseMapper = baseMapper;
    this.example = example;
    this.current = example.createCriteria();
  }

  /**
   * or 一组条件
   *
   * @return 条件
   */
  public ExampleWrapper<T, I> or() {
    this.current = this.example.or();
    return this;
  }

  /**
   * 获取查询条件
   */
  public Example<T> example() {
    return example;
  }

  /**
   * 指定查询列
   *
   * @param fns 方法引用
   */
  @SafeVarargs
  public final ExampleWrapper<T, I> select(Fn<T, Object>... fns) {
    this.example.selectColumns(fns);
    return this;
  }

  /**
   * 设置起始 SQL
   *
   * @param startSql 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  public ExampleWrapper<T, I> startSql(String startSql) {
    this.example.setStartSql(startSql);
    return this;
  }

  /**
   * 设置结尾 SQL
   *
   * @param endSql 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  public ExampleWrapper<T, I> endSql(String endSql) {
    this.example.setEndSql(endSql);
    return this;
  }

  /**
   * 通过方法引用方式设置排序字段
   *
   * @param fn    排序列的方法引用
   * @param order 排序方式
   * @return Example
   */
  public ExampleWrapper<T, I> orderBy(Fn<T, Object> fn, Example.Order order) {
    this.example.orderBy(fn, order);
    return this;
  }

  /**
   * 通过方法引用方式设置排序字段，升序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final ExampleWrapper<T, I> orderByAsc(Fn<T, Object>... fns) {
    this.example.orderByAsc(fns);
    return this;
  }

  /**
   * 通过方法引用方式设置排序字段，降序排序
   *
   * @param fns 排序列的方法引用
   * @return Example
   */
  @SafeVarargs
  public final ExampleWrapper<T, I> orderByDesc(Fn<T, Object>... fns) {
    this.example.orderByDesc(fns);
    return this;
  }

  /**
   * 设置 distince
   */
  public ExampleWrapper<T, I> distinct() {
    this.example.setDistinct(true);
    return this;
  }

  /**
   * 指定字段为 null
   *
   * @param fn 字段对应的 get 方法引用
   */
  public ExampleWrapper<T, I> isNull(Fn<T, Object> fn) {
    this.current.addCriterion(fn.toColumn() + " IS NULL");
    return this;
  }

  /**
   * 指定字段不为 null
   *
   * @param fn 字段对应的 get 方法引用
   */
  public ExampleWrapper<T, I> isNotNull(Fn<T, Object> fn) {
    this.current.addCriterion(fn.toColumn() + " IS NOT NULL");
    return this;
  }

  /**
   * 字段 = 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> eq(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " =", value);
    return this;
  }

  /**
   * 字段 != 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> ne(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <>", value);
    return this;
  }

  /**
   * 字段 > 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> gt(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " >", value);
    return this;
  }

  /**
   * 字段 >= 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> ge(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " >=", value);
    return this;
  }

  /**
   * 字段 < 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> lt(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <", value);
    return this;
  }

  /**
   * 字段 <= 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值
   */
  public ExampleWrapper<T, I> le(Fn<T, Object> fn, Object value) {
    this.current.addCriterion(fn.toColumn() + " <=", value);
    return this;
  }

  /**
   * 字段 in (值集合)
   *
   * @param fn     字段对应的 get 方法引用
   * @param values 值集合
   */
  @SuppressWarnings("rawtypes")
  public ExampleWrapper<T, I> in(Fn<T, Object> fn, Iterable values) {
    this.current.addCriterion(fn.toColumn() + " IN", values);
    return this;
  }

  /**
   * 字段 not in (值集合)
   *
   * @param fn     字段对应的 get 方法引用
   * @param values 值集合
   */
  @SuppressWarnings("rawtypes")
  public ExampleWrapper<T, I> notIn(Fn<T, Object> fn, Iterable values) {
    this.current.addCriterion(fn.toColumn() + " NOT IN", values);
    return this;
  }

  /**
   * 字段 between value1 and value 2
   *
   * @param fn     字段对应的 get 方法引用
   * @param value1 值1
   * @param value2 值2
   */
  public ExampleWrapper<T, I> between(Fn<T, Object> fn, Object value1, Object value2) {
    this.current.addCriterion(fn.toColumn() + " BETWEEN", value1, value2);
    return this;
  }

  /**
   * 字段 not between value1 and value 2
   *
   * @param fn     字段对应的 get 方法引用
   * @param value1 值1
   * @param value2 值2
   */
  public ExampleWrapper<T, I> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
    this.current.addCriterion(fn.toColumn() + " NOT BETWEEN", value1, value2);
    return this;
  }

  /**
   * 字段 like %值%
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，两侧自动添加 %
   */
  public ExampleWrapper<T, I> contains(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", "%" + value + "%");
    return this;
  }

  /**
   * 字段 like 值%，匹配 value 为前缀的值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，右侧自动添加 %
   */
  public ExampleWrapper<T, I> startsWith(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", value + "%");
    return this;
  }

  /**
   * 字段 like %值，匹配 value 为后缀的值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，左侧自动添加 %
   */
  public ExampleWrapper<T, I> endsWith(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", "%" + value);
    return this;
  }

  /**
   * 字段 like 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，需要指定 '%'(多个), '_'(单个) 模糊匹配
   */
  public ExampleWrapper<T, I> like(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  LIKE", value);
    return this;
  }

  /**
   * 字段 not like 值
   *
   * @param fn    字段对应的 get 方法引用
   * @param value 值，需要指定 % 模糊匹配
   */
  public ExampleWrapper<T, I> notLike(Fn<T, Object> fn, String value) {
    this.current.addCriterion(fn.toColumn() + "  NOT LIKE", value);
    return this;
  }

  /**
   * 添加任意条件，条件一定是后端使用的，需要自己防止 SQL 注入
   *
   * @param condition 任意条件，例如 "length(countryname)<5"
   */
  public ExampleWrapper<T, I> anyCondition(String condition) {
    this.current.andCondition(condition);
    return this;
  }

  /**
   * 手写左边条件，右边用value值
   *
   * @param condition 例如 "length(countryname)="
   * @param value     例如 5
   */
  public ExampleWrapper<T, I> anyCondition(String condition, Object value) {
    this.current.andCondition(condition, value);
    return this;
  }

  /**
   * 嵌套 or 查询，数组多个条件直接使用 or，单个条件中为 and
   *
   * @param orParts 条件块
   */
  @SafeVarargs
  public final ExampleWrapper<T, I> or(Function<Example.OrCriteria<T>, Example.OrCriteria<T>>... orParts) {
    if (orParts != null && orParts.length > 0) {
      this.current.andOr(Arrays.stream(orParts).map(orPart -> orPart.apply(example.orPart())).collect(Collectors.toList()));
    }
    return this;
  }

  /**
   * 根据当前条件删除
   */
  public int delete() {
    return baseMapper.deleteByExample(example);
  }

  /**
   * 将符合当前查询条件的数据更新为提供的值
   *
   * @param t 要更新的值
   */
  public int update(T t) {
    return baseMapper.updateByExample(t, example);
  }

  /**
   * 将符合当前查询条件的数据更新为提供的值，不更新 null 的值
   *
   * @param t 要更新的值
   */
  public int updateSelective(T t) {
    return baseMapper.updateByExampleSelective(t, example);
  }

  /**
   * 根据当前查询条件查询
   */
  public List<T> list() {
    return baseMapper.selectByExample(example);
  }

  /**
   * 根据当前查询条件查询，返回 Stream
   */
  public Stream<T> stream() {
    return list().stream();
  }

  /**
   * 根据当前查询条件查询出一个结果，当存在多个符合条件的结果时会抛出异常 {@link TooManyResultsException}
   */
  public Optional<T> one() {
    return baseMapper.selectOneByExample(example);
  }

  /**
   * 根据当前查询条件查询出第一个结果，通过 {@link RowBounds} 实现
   */
  public Optional<T> first() {
    return baseMapper.selectByExample(example, new RowBounds(0, 1)).stream().findFirst();
  }

  /**
   * 根据当前查询条件查询出前 n 个结果，通过 {@link RowBounds} 实现
   *
   * @param n 结果数
   */
  public List<T> top(int n) {
    return baseMapper.selectByExample(example, new RowBounds(0, n));
  }

  /**
   * 查询符合当前条件的结果数
   */
  public long count() {
    return baseMapper.countByExample(example);
  }

}
