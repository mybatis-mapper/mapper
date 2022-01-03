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

import io.mybatis.common.util.Assert;
import io.mybatis.mapper.BaseMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

import static io.mybatis.common.core.Code.DELETE_FAILURE;
import static io.mybatis.common.core.Code.SAVE_FAILURE;
import static io.mybatis.common.core.Code.UPDATE_FAILURE;

/**
 * 基础方法实现，推荐自己的实现类继承该类
 * <p>
 * 例如自己的接口：
 * <pre>
 *   public interface UserService {
 *     User save(User user);
 *   }
 * </pre>
 * 对应的实现类:
 * <pre>
 *   public class UserServiceImpl extends AbstractService&lt;User, Long, UserMapper&gt; implements UserService {
 *       //由于 User save(User user); 和默认的 T save(T entity) 方法一致，所以不需要提供实现，可以用默认方法
 *   }
 * </pre>
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @param <M> Mapper类型
 * @author liuzh
 */
public abstract class AbstractService<T, I extends Serializable, M extends BaseMapper<T, I>>
    implements BaseService<T, I> {

  protected M baseMapper;

  @Autowired
  public void setBaseMapper(M baseMapper) {
    this.baseMapper = baseMapper;
  }

  @Override
  public T save(T entity) {
    Assert.isTrue(baseMapper.insert(entity) == 1, SAVE_FAILURE);
    return entity;
  }

  @Override
  public T saveSelective(T entity) {
    Assert.isTrue(baseMapper.insertSelective(entity) == 1, SAVE_FAILURE);
    return entity;
  }

  @Override
  public T update(T entity) {
    Assert.isTrue(baseMapper.updateByPrimaryKey(entity) == 1, UPDATE_FAILURE);
    return entity;
  }

  @Override
  public T updateSelective(T entity) {
    Assert.isTrue(baseMapper.updateByPrimaryKeySelective(entity) == 1, UPDATE_FAILURE);
    return entity;
  }

  @Override
  public T updateSelective(T entity, Fn<T, Object>... forceUpdateFields) {
    Assert.isTrue(baseMapper.updateByPrimaryKeySelectiveWithForceFields(
        entity, Fn.of(forceUpdateFields)) == 1, UPDATE_FAILURE);
    return entity;
  }

  @Override
  public boolean pkHasValue(T entity) {
    EntityTable entityTable = baseMapper.entityTable();
    List<EntityColumn> idColumns = entityTable.idColumns();
    return idColumns.get(0).field().get(entity) != null;
  }

  @Override
  public T saveOrUpdate(T entity) {
    if (pkHasValue(entity)) {
      return update(entity);
    } else {
      return save(entity);
    }
  }

  @Override
  public T saveOrUpdateSelective(T entity) {
    if (pkHasValue(entity)) {
      return updateSelective(entity);
    } else {
      return saveSelective(entity);
    }
  }

  @Override
  public int delete(T entity) {
    return baseMapper.delete(entity);
  }

  @Override
  public int deleteById(I id) {
    int count = baseMapper.deleteByPrimaryKey(id);
    Assert.isTrue(count == 1, DELETE_FAILURE);
    return count;
  }

  @Override
  public <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    return baseMapper.deleteByFieldList(field, fieldValueList);
  }

  /**
   * 根据指定的主键查询
   *
   * @param id 主键
   * @return 实体
   */
  @Override
  public T findById(I id) {
    return baseMapper.selectByPrimaryKey(id).orElse(null);
  }

  /**
   * 以当前类作为条件查询一个，当结果多于1个时出错
   *
   * @param entity 实体类
   * @return 实体
   */
  @Override
  public T findOne(T entity) {
    return baseMapper.selectOne(entity).orElse(null);
  }

  /**
   * 以当前类作为条件查询
   *
   * @param entity 实体类
   * @return 实体集合
   */
  @Override
  public List<T> findList(T entity) {
    return baseMapper.selectList(entity);
  }

  /**
   * 根据指定字段集合查询
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 实体集合
   */
  @Override
  public <F> List<T> findByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    return baseMapper.selectByFieldList(field, fieldValueList);
  }

  /**
   * 查询全部
   *
   * @return 实体集合
   */
  @Override
  public List<T> findAll() {
    return baseMapper.selectList(null);
  }

  /**
   * 以当前类作为条件查询总数
   *
   * @param entity 实体类
   * @return 实体集合
   */
  @Override
  public long count(T entity) {
    return baseMapper.selectCount(entity);
  }

  /**
   * 根据 example 条件批量删除
   *
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  @Override
  public int delete(Example<T> example) {
    return baseMapper.deleteByExample(example);
  }

  /**
   * 根据 example 查询条件批量更新（所有字段）
   *
   * @param entity  实体类
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  @Override
  public int update(T entity, Example<T> example) {
    return baseMapper.updateByExample(entity, example);
  }

  /**
   * 根据 example 查询条件批量更新（非空字段）
   *
   * @param entity  实体类
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  @Override
  public int updateSelective(T entity, Example<T> example) {
    return baseMapper.updateByExampleSelective(entity, example);
  }

  /**
   * 根据 example 条件查询一个，当结果多于1个时出错
   *
   * @param example 查询条件
   * @return 实体
   */
  @Override
  public T findOne(Example<T> example) {
    return baseMapper.selectOneByExample(example).orElse(null);
  }

  /**
   * 根据 example 条件查询
   *
   * @param example 查询条件
   * @return 实体集合
   */
  @Override
  public List<T> findList(Example<T> example) {
    return baseMapper.selectByExample(example);
  }

  /**
   * 根据 example 查询总数
   *
   * @param example 查询条件
   * @return 总数
   */
  @Override
  public long count(Example<T> example) {
    return baseMapper.countByExample(example);
  }
}
