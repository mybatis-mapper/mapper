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

package io.mybatis.activerecord;

import io.mybatis.common.util.Assert;
import io.mybatis.mapper.Mapper;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;

import java.io.Serializable;
import java.util.List;

import static io.mybatis.common.core.Code.*;

/**
 * 建议将继承该抽象类的实现类的作用范围控制在 Service 层，不能超出范围，其它层使用时转换为 VO 或 DTO 后使用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface EntityRecord<T, I extends Serializable> extends MapperRecord<T, I, Mapper<T, I>> {

  /**
   * 保存（所有字段）
   */
  default void save() {
    Assert.isTrue(baseMapper().insert((T) this) == 1, SAVE_FAILURE);
  }

  /**
   * 保存（非空字段，空的字段会使用数据库设置的默认值，但是不会字段反写）
   */
  default void saveSelective() {
    Assert.isTrue(baseMapper().insertSelective((T) this) == 1, SAVE_FAILURE);
  }

  /**
   * 根据主键更新（所有字段）
   */
  default void update() {
    Assert.isTrue(baseMapper().updateByPrimaryKey((T) this) == 1, UPDATE_FAILURE);
  }

  /**
   * 根据主键更新（非空字段）
   */
  default void updateSelective() {
    Assert.isTrue(baseMapper().updateByPrimaryKeySelective((T) this) == 1, UPDATE_FAILURE);
  }

  /**
   * 根据主键更新（非空字段），指定的强制更新字段不区分是否为空
   *
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null
   * @return 返回 1成功，0失败
   */
  default void updateSelective(Fn<T, Object>... forceUpdateFields) {
    Assert.isTrue(baseMapper()
        .updateByPrimaryKeySelectiveWithForceFields((T) this, Fn.of(forceUpdateFields)) == 1, UPDATE_FAILURE);
  }

  /**
   * 返回主键值，建议子类替换为效率更高的判断方式（例如主键为 id 的情况下，直接 return id）
   *
   * @return 主键值
   */
  default I pkValue() {
    EntityTable entityTable = baseMapper().entityTable();
    List<EntityColumn> idColumns = entityTable.idColumns();
    if (idColumns.size() == 1) {
      return (I) idColumns.get(0).field().get(this);
    } else {
      return idColumns.get(0).field().get(this) != null ? (I) this : null;
    }
  }

  /**
   * 主键是否有值
   *
   * @return true有值，false为空
   */
  default boolean pkHasValue() {
    return pkValue() != null;
  }

  /**
   * 保存或更新（全部字段），当主键不存在时保存，存在时更新
   *
   * @return 返回 1成功，0失败
   */
  default void saveOrUpdate() {
    if (pkHasValue()) {
      update();
    } else {
      save();
    }
  }

  /**
   * 保存或更新（非空字段），当主键不存在时保存，存在时更新
   *
   * @return 返回 1成功，0失败
   */
  default void saveOrUpdateSelective() {
    if (pkHasValue()) {
      updateSelective();
    } else {
      saveSelective();
    }
  }

  /**
   * 根据当前类的值作为条件进行删除（注意：当所有字段都没有值时可能会清库）
   *
   * @return 返回大于 1成功，0失败
   */
  default int delete() {
    return baseMapper().delete((T) this);
  }

  /**
   * 根据主键进行删除
   *
   * @return 返回 1成功，0失败
   */
  default void deleteById() {
    Assert.isTrue(baseMapper().deleteByPrimaryKey(pkValue()) == 1, DELETE_FAILURE);
  }

  /**
   * 根据指定的主键进行删除
   *
   * @param id 指定的主键
   * @return 返回 1成功，0失败
   */
  default void deleteById(I id) {
    Assert.isTrue(baseMapper().deleteByPrimaryKey(id) == 1, DELETE_FAILURE);
  }

  /**
   * 根据指定字段集合删除
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 删除数据的条数
   */
  default <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    return baseMapper().deleteByFieldList(field, fieldValueList);
  }

  /**
   * 根据指定的主键查询
   *
   * @param id 主键
   * @return 实体
   */
  default T findById(I id) {
    return baseMapper().selectByPrimaryKey(id).orElse(null);
  }

  /**
   * 以当前类作为条件查询一个，当结果多于1个时出错
   *
   * @return 实体
   */
  default T findOne() {
    return baseMapper().selectOne((T) this).orElse(null);
  }

  /**
   * 以当前类作为条件查询
   *
   * @return 实体集合
   */
  default List<T> findList() {
    return baseMapper().selectList((T) this);
  }

  /**
   * 根据指定字段集合查询
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 实体集合
   */
  default <F> List<T> findByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    return baseMapper().selectByFieldList(field, fieldValueList);
  }

  /**
   * 查询全部
   *
   * @return 实体集合
   */
  default List<T> findAll() {
    return baseMapper().selectList(null);
  }

  /**
   * 以当前类作为条件查询总数
   *
   * @return 实体集合
   */
  default long count() {
    return baseMapper().selectCount((T) this);
  }

}
