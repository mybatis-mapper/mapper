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

package io.mybatis.provider.jpa;

import io.mybatis.provider.defaults.GenericEntityClassFinder;

import javax.persistence.Table;

/**
 * 支持识别带有 @javax.persistence.Table 的实体类
 *
 * @author liuzh
 */
public class JpaEntityClassFinder extends GenericEntityClassFinder {

  @Override
  public boolean isEntityClass(Class<?> clazz) {
    return clazz.isAnnotationPresent(Table.class);
  }

  @Override
  public int getOrder() {
    return super.getOrder() + 100;
  }
}
