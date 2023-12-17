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

import io.mybatis.common.util.Assert;
import io.mybatis.mapper.fn.Fn;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 封装 Example 的查询条件，方便链式调用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public class ExampleWrapper<T, I extends Serializable> extends AbstractExampleWrapper<T, ExampleWrapper<T, I>> {
  private final ExampleMapper<T, Example<T>> exampleMapper;

  public ExampleWrapper(ExampleMapper<T, Example<T>> exampleMapper, Example<T> example) {
    super(example);
    this.exampleMapper = exampleMapper;
  }

  @Override
  public ExampleWrapper<T, I> getSelf() {
    return this;
  }

  /**
   * 根据当前条件删除
   */
  public int delete() {
    return exampleMapper.deleteByExample(super.example());
  }

  /**
   * 将符合当前查询条件的数据更新为 {@link #set(String)} 和 {@link #set(Fn, Object)} 设置的值
   */
  public int update() {
    Assert.notEmpty(super.example().getSetValues(), "必须通过 set 方法设置更新的列和值");
    return exampleMapper.updateByExampleSetValues(super.example());
  }

  /**
   * 将符合当前查询条件的数据更新为提供的值, {@link #set(String)} 和 {@link #set(Fn, Object)} 设置的值无效
   *
   * @param t 要更新的值
   */
  public int update(T t) {
    return exampleMapper.updateByExample(t, super.example());
  }

  /**
   * 将符合当前查询条件的数据更新为提供的值，不更新 null 的值
   *
   * @param t 要更新的值
   */
  public int updateSelective(T t) {
    return exampleMapper.updateByExampleSelective(t, super.example());
  }

  /**
   * 根据当前查询条件查询
   */
  public List<T> list() {
    return exampleMapper.selectByExample(super.example());
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
    return exampleMapper.selectOneByExample(super.example());
  }

  /**
   * 根据当前查询条件查询出第一个结果，通过 {@link RowBounds} 实现
   */
  public Optional<T> first() {
    return exampleMapper.selectByExample(super.example(), new RowBounds(0, 1)).stream().findFirst();
  }

  /**
   * 根据当前查询条件查询出前 n 个结果，通过 {@link RowBounds} 实现
   *
   * @param n 结果数
   */
  public List<T> top(int n) {
    return exampleMapper.selectByExample(super.example(), new RowBounds(0, n));
  }

  /**
   * 查询符合当前条件的结果数
   */
  public long count() {
    return exampleMapper.countByExample(super.example());
  }

}
