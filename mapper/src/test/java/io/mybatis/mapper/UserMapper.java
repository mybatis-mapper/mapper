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

package io.mybatis.mapper;

import io.mybatis.mapper.base.EntityMapper;
import io.mybatis.mapper.base.EntityProvider;
import io.mybatis.mapper.cursor.CursorMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.example.ExampleMapper;
import io.mybatis.mapper.fn.FnMapper;
import io.mybatis.mapper.list.ListMapper;
import io.mybatis.mapper.model.User;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper extends
    EntityMapper<User, Long>,
    CursorMapper<User, Example<User>>,
    FnMapper<User>,
    ExampleMapper<User, Example<User>>,
    ListMapper<User>,
    SumMapper<User> {

  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(User entity);

  @Override
  @Lang(Caching.class)
  @SelectKey(statement = "CALL IDENTITY()", keyProperty = "id", resultType = Long.class, before = false)
  @InsertProvider(type = EntityProvider.class, method = "insertSelective")
  int insertSelective(User entity);

  default int deleteByIds(List<Long> ids) {
    Example<User> example = new Example<>();
    example.createCriteria().andIn(User::getId, ids);
    return deleteByExample(example);
  }

  /**
   * 不支持的方法
   */
  @SelectProvider(type = EntityProvider.class, method = "unsupported")
  int unsupported();

}
