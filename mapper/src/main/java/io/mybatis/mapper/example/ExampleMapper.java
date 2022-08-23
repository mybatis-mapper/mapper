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

import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Optional;

/**
 * Example 相关方法
 *
 * @param <T> 实体类
 * @param <E> 符合Example数据结构的对象，例如 {@link Example}，也可以是 MBG 生成 XXXExample 对象。
 * @author liuzh
 */
public interface ExampleMapper<T, E> {

  /**
   * 获取 Example 对象
   *
   * @return Example 对象
   */
  default Example<T> example() {
    return new Example<>();
  }

  /**
   * 根据 Example 删除
   *
   * @param example 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = ExampleProvider.class, method = "deleteByExample")
  int deleteByExample(E example);

  /**
   * 根据 Example 条件批量更新实体信息
   *
   * @param entity  实体类
   * @param example 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = ExampleProvider.class, method = "updateByExample")
  int updateByExample(@Param("entity") T entity, @Param("example") E example);

  /**
   * 根据 Example 条件和 setValue 值更新字段
   *
   * @param example 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = ExampleProvider.class, method = "updateByExampleSetValues")
  int updateByExampleSetValues(@Param("example") E example);

  /**
   * 根据 Example 条件批量更新实体不为空的字段
   *
   * @param entity  实体类
   * @param example 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = ExampleProvider.class, method = "updateByExampleSelective")
  int updateByExampleSelective(@Param("entity") T entity, @Param("example") E example);

  /**
   * 根据 Example 条件批量查询
   *
   * @param example 条件
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = ExampleProvider.class, method = "selectByExample")
  List<T> selectByExample(E example);

  /**
   * 根据 Example 条件查询单个实体
   *
   * @param example 条件
   * @return 单个实体，查询结果由多条时报错
   */
  @Lang(Caching.class)
  @SelectProvider(type = ExampleProvider.class, method = "selectByExample")
  Optional<T> selectOneByExample(E example);

  /**
   * 根据 Example 条件查询总数
   *
   * @param example 条件
   * @return 总数
   */
  @Lang(Caching.class)
  @SelectProvider(type = ExampleProvider.class, method = "countByExample")
  long countByExample(E example);

  /**
   * 根据 Example 条件批量查询
   *
   * @param example   条件
   * @param rowBounds 分页信息
   * @return 实体列表
   */
  List<T> selectByExample(E example, RowBounds rowBounds);

}
