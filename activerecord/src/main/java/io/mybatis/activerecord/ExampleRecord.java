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

import io.mybatis.mapper.Mapper;
import io.mybatis.mapper.example.Example;

import java.io.Serializable;
import java.util.List;

/**
 * 建议将继承该抽象类的实现类的作用范围控制在 Service 层，不能超出范围，其它层使用时转换为 VO 或 DTO 后使用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface ExampleRecord<T, I extends Serializable> extends MapperRecord<T, I, Mapper<T, I>> {
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
  default int delete(Example<T> example) {
    return baseMapper().deleteByExample(example);
  }

  /**
   * 根据 example 查询条件批量更新（所有字段）
   *
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  default int update(Example<T> example) {
    return baseMapper().updateByExample((T) this, example);
  }

  /**
   * 根据 example 查询条件批量更新（非空字段）
   *
   * @param example 查询条件
   * @return 返回大于0成功，0失败
   */
  default int updateSelective(Example<T> example) {
    return baseMapper().updateByExampleSelective((T) this, example);
  }

  /**
   * 根据 example 条件查询一个，当结果多于1个时出错
   *
   * @param example 查询条件
   * @return 实体
   */
  default T findOne(Example<T> example) {
    return baseMapper().selectOneByExample(example).orElse(null);
  }

  /**
   * 根据 example 条件查询
   *
   * @param example 查询条件
   * @return 实体集合
   */
  default List<T> findList(Example<T> example) {
    return baseMapper().selectByExample(example);
  }

  /**
   * 根据 example 查询总数
   *
   * @param example 查询条件
   * @return 总数
   */
  default long count(Example<T> example) {
    return baseMapper().countByExample(example);
  }

}
