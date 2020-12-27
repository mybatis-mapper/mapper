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

package io.mybatis.service;

import io.mybatis.mapper.fn.Fn;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 实体类基本接口
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface EntityService<T, I extends Serializable> {

  /**
   * 保存（所有字段）
   *
   * @param entity 实体类
   * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T save(T entity);

  /**
   * 保存（非空字段，空的字段会使用数据库设置的默认值，但是不会字段反写）
   *
   * @param entity 实体类
   * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T saveSelective(T entity);

  /**
   * 更新（所有字段）
   *
   * @param entity 实体类
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T update(T entity);

  /**
   * 更新（非空字段）
   *
   * @param entity 实体类
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T updateSelective(T entity);

  /**
   * 更新（非空字段），指定的强制更新字段不区分是否为空
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T updateSelective(T entity, Fn<T, Object>... forceUpdateFields);

  /**
   * 主键是否有值
   *
   * @param entity 实体类
   * @return true有值，false为空
   */
  boolean pkHasValue(T entity);

  /**
   * 保存或更新（全部字段），当主键不存在时保存，存在时更新
   *
   * @param entity 实体类
   * @return 返回保存或更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T saveOrUpdate(T entity);

  /**
   * 保存或更新（非空字段），当主键不存在时保存，存在时更新
   *
   * @param entity 实体类
   * @return 返回保存或更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  T saveOrUpdateSelective(T entity);

  /**
   * 根据当前类的值作为条件进行删除（注意：当所有字段都没有值时可能会清库）
   *
   * @param entity 实体类
   * @return 返回大于 1成功，0失败
   */
  int delete(T entity);

  /**
   * 根据主键进行删除
   *
   * @param id 指定的主键
   * @return 返回 1成功，0失败抛出异常
   */
  int deleteById(I id);

  /**
   * 根据指定字段集合删除
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 删除数据的条数
   */
  <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList);

  /**
   * 根据指定的主键查询
   *
   * @param id 主键
   * @return 实体
   */
  Optional<T> findById(I id);

  /**
   * 以当前类作为条件查询一个，当结果多于1个时出错
   *
   * @param entity 实体类
   * @return 实体
   */
  Optional<T> findOne(T entity);

  /**
   * 以当前类作为条件查询
   *
   * @param entity 实体类
   * @return 实体集合
   */
  List<T> findList(T entity);

  /**
   * 根据指定字段集合查询
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 实体集合
   */
  <F> List<T> findByFieldList(Fn<T, F> field, List<F> fieldValueList);

  /**
   * 查询全部
   *
   * @return 实体集合
   */
  List<T> findAll();

  /**
   * 以当前类作为条件查询总数
   *
   * @param entity 实体类
   * @return 实体集合
   */
  long count(T entity);

}
