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
import io.mybatis.service.model.Role;

import java.util.Collection;

public interface RoleService {

  /**
   * 保存（所有字段）
   *
   * @param entity 实体类
   * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role save(Role entity);

  /**
   * 更新（所有字段）
   *
   * @param entity 实体类
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role update(Role entity);

  /**
   * 更新（非空字段），指定的强制更新字段不区分是否为空
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role updateSelective(Role entity, Fn<Role, Object>... forceUpdateFields);

  /**
   * 根据主键进行删除
   *
   * @param id 指定的主键
   * @return 返回 1成功，0失败抛出异常
   */
  int deleteById(Integer id);

  /**
   * 根据指定字段集合删除
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 删除数据的条数
   */
  <F> int deleteByFieldList(Fn<Role, F> field, Collection<F> fieldValueList);

  /**
   * 根据指定的主键查询
   *
   * @param id 主键
   * @return 实体
   */
  Role findById(Integer id);

}
