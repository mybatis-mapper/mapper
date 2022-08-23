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

import io.mybatis.activerecord.ActiveRecord;
import io.mybatis.mapper.BaseMapper;
import io.mybatis.provider.Entity;

@Entity.Table
public class BaseId<T extends BaseId> implements ActiveRecord<T, Integer> {
  @Entity.Column(id = true, insertable = false)
  private Integer id;

  @Override
  public Integer pkValue() {
    return id;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public BaseMapper<T, Integer> baseMapper() {
    //获取指定数据源的实例
    return MapperProvider.<T, Integer, BaseMapper<T, Integer>>getInstance("mapperProviderUser").baseMapper(entityClass());
  }
}
