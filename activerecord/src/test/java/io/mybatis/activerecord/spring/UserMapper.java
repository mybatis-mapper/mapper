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
package io.mybatis.activerecord.spring;

import io.mybatis.mapper.base.EntityProvider;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;

public interface UserMapper extends CustomMapper<User>, UserMarker {
  /**
   * 保存实体，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(User entity);

  User findById(Long id);

}
