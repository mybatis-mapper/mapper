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

package io.mybatis.service.impl;

import io.mybatis.mapper.BaseMapper;
import io.mybatis.service.AbstractService;
import io.mybatis.service.model.BaseId;

import java.util.List;

public abstract class BaseIdService<T extends BaseId<T>, M extends BaseMapper<T, Integer>> extends AbstractService<T, Integer, M> {

  /**
   * 根据ID列表进行删除，issues #50
   */
  public int deleteByIdList(List<Integer> ids) {
    return deleteByFieldList(T::getId, ids);
  }

}
