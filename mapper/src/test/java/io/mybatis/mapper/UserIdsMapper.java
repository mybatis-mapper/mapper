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

package io.mybatis.mapper;

import io.mybatis.mapper.base.EntityMapper;
import io.mybatis.mapper.base.EntityProvider;
import io.mybatis.mapper.cursor.CursorMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.example.ExampleMapper;
import io.mybatis.mapper.fn.FnMapper;
import io.mybatis.mapper.list.ListMapper;
import io.mybatis.mapper.model.UserIds;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;

public interface UserIdsMapper extends
    EntityMapper<UserIds, UserIds>,
    FnMapper<UserIds>,
    CursorMapper<UserIds, Example<UserIds>>,
    ExampleMapper<UserIds, Example<UserIds>>,
    ListMapper<UserIds> {

  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(UserIds entity);

  @Override
  @Lang(Caching.class)
  @InsertProvider(type = EntityProvider.class, method = "insertSelective")
  int insertSelective(UserIds entity);

}
