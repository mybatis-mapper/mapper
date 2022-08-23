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

package io.mybatis.mapper.fn;

import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Optional;

/**
 * 可指定字段的方法
 *
 * @param <T> 实体类类型
 * @author liuzh
 */
public interface FnMapper<T> {

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否 null，都更新
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = FnProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
  int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);

  /**
   * 根据实体字段条件查询唯一的实体（{@link io.mybatis.mapper.example.ExampleMapper} 可以实现一样的功能，当前方法只是示例）
   *
   * @param entity       实体类
   * @param selectFileds 查询的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 单个实体，查询结果由多条时报错
   */
  @Lang(Caching.class)
  @SelectProvider(type = FnProvider.class, method = "selectColumns")
  Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFileds);

  /**
   * 根据实体字段条件批量查询（{@link io.mybatis.mapper.example.ExampleMapper} 可以实现一样的功能，当前方法只是示例）
   *
   * @param entity       实体类
   * @param selectFileds 查询的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = FnProvider.class, method = "selectColumns")
  List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFileds);

}
