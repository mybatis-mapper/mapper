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

package io.mybatis.mapper.base;

import io.mybatis.provider.Caching;
import io.mybatis.provider.EntityInfoMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 实体类基本方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface EntityMapper<T, I> extends EntityInfoMapper<T> {

  /**
   * 保存实体
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = EntityProvider.class, method = "insert")
  <S extends T> int insert(S entity);

  /**
   * 保存实体中不为空的字段
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = EntityProvider.class, method = "insertSelective")
  <S extends T> int insertSelective(S entity);

  /**
   * 根据主键删除
   *
   * @param id 主键
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = EntityProvider.class, method = "deleteByPrimaryKey")
  int deleteByPrimaryKey(I id);

  /**
   * 保存实体信息批量删除
   *
   * @param entity 实体类
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = EntityProvider.class, method = "delete")
  int delete(T entity);

  /**
   * 根据主键更新实体
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKey")
  <S extends T> int updateByPrimaryKey(S entity);

  /**
   * 根据主键更新实体中不为空的字段
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKeySelective")
  <S extends T> int updateByPrimaryKeySelective(S entity);

  /**
   * 根据主键查询实体
   *
   * @param id 主键
   * @return 实体
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "selectByPrimaryKey")
  Optional<T> selectByPrimaryKey(I id);

  /**
   * 根据实体字段条件查询唯一的实体
   *
   * @param entity 实体类
   * @return 单个实体，查询结果由多条时报错
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "select")
  Optional<T> selectOne(T entity);

  /**
   * 根据实体字段条件批量查询
   *
   * @param entity 实体类
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "select")
  List<T> selectList(T entity);

  /**
   * 根据实体字段条件查询总数
   *
   * @param entity 实体类
   * @return 总数
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "selectCount")
  long selectCount(T entity);

  /* 下面的方法不需要额外的实现，算是默认方法的示例，不直接提供此类方法 */
  /*  *//**
   * 根据实体字段条件分页查询
   *
   * @param entity    实体类
   * @param rowBounds 分页信息
   * @return 实体列表
   *//*
  List<T> selectList(T entity, RowBounds rowBounds);

  *//**
   * 根据查询条件获取第一个结果
   *
   * @param entity 实体类
   * @return 实体
   *//*
  default Optional<T> selectFirst(T entity) {
    List<T> entities = selectList(entity, new RowBounds(0, 1));
    if (entities.size() == 1) {
      return Optional.of(entities.get(0));
    }
    return Optional.empty();
  }

  *//**
   * 根据查询条件获取指定的前几个对象
   *
   * @param entity 实体类
   * @param n      指定的个数
   * @return 实体
   *//*
  default List<T> selectTopN(T entity, int n) {
    return selectList(entity, new RowBounds(0, n));
  }*/

}
