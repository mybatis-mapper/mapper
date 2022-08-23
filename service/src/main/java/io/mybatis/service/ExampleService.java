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

package io.mybatis.service;

import io.mybatis.mapper.example.Example;

import java.io.Serializable;
import java.util.List;

/**
 * Example 接口
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface ExampleService<T, I extends Serializable> {

  /**
   * 获取 Example 对象
   *
   * @return Example 对象
   */
  default Example<T> example() {
    return new Example<>();
  }

  /**
   * 根据 example 条件批量删除
   *
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  int delete(Example<T> example);

  /**
   * 根据 example 查询条件批量更新（所有字段）
   *
   * @param entity  实体类
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  int update(T entity, Example<T> example);

  /**
   * 根据 example 查询条件批量更新（非空字段）
   *
   * @param entity  实体类
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  int updateSelective(T entity, Example<T> example);

  /**
   * 根据 example 条件查询一个，当结果多于1个时出错
   *
   * @param example 查询条件
   * @return 实体
   */
  T findOne(Example<T> example);

  /**
   * 根据 example 条件查询
   *
   * @param example 查询条件
   * @return 实体集合
   */
  List<T> findList(Example<T> example);

  /**
   * 根据 example 查询总数
   *
   * @param example 查询条件
   * @return 总数
   */
  long count(Example<T> example);

}
