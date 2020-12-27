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

package io.mybatis.mapper.cursor;

import io.mybatis.mapper.base.EntityProvider;
import io.mybatis.mapper.example.ExampleProvider;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.cursor.Cursor;

/**
 * 游标查询方法
 *
 * @param <T> 实体类
 * @param <E> 符合Example数据结构的对象，例如 {@link io.mybatis.mapper.example.Example}，也可以是 MBG 生成 XXXExample 对象。
 * @author liuzh
 */
public interface CursorMapper<T, E> {

  /**
   * 根据实体字段条件查询
   *
   * @param entity 实体类
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "select")
  Cursor<T> selectCursor(T entity);

  /**
   * 根据 Example 条件查询
   *
   * @param example 条件
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = ExampleProvider.class, method = "selectByExample")
  Cursor<T> selectCursorByExample(E example);

}
