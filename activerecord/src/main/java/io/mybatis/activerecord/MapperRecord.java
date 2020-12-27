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

import io.mybatis.activerecord.spring.MapperProvider;
import io.mybatis.mapper.Mapper;
import io.mybatis.provider.EntityInfoMapper;

import java.io.Serializable;

/**
 * 提供通用 Mapper
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @param <M> 实体对应的 Mapper
 * @author liuzh
 */
public interface MapperRecord<T, I extends Serializable, M extends Mapper<T, I>> extends EntityInfoMapper<T> {

  /**
   * 通用 Mapper
   *
   * @return 通用 Mapper
   */
  default M baseMapper() {
    return MapperProvider.<T, I, M>getDefaultInstance().baseMapper(entityClass());
  }

}
