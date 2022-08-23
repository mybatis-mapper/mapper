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

package io.mybatis.provider.jpa;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.EntityTableFactory;
import io.mybatis.provider.Style;
import io.mybatis.provider.util.Utils;

import javax.persistence.Table;

/**
 * 通过 SPI 工厂扩展 EntityColumn 和 EntityTable
 *
 * @author liuzh
 */
public class JpaEntityTableFactory implements EntityTableFactory {

  @Override
  public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
    EntityTable entityTable = chain.createEntityTable(entityClass);
    if (entityTable == null) {
      entityTable = EntityTable.of(entityClass);
    }
    if (entityClass.isAnnotationPresent(Table.class)) {
      Table table = entityClass.getAnnotation(Table.class);
      if (!table.name().isEmpty()) {
        entityTable.table(table.name());
      }
    } else if (Utils.isEmpty(entityTable.table())) {
      //没有设置表名时，默认类名转下划线
      entityTable.table(Style.getDefaultStyle().tableName(entityClass));
    }
    return entityTable;
  }

  @Override
  public int getOrder() {
    return EntityTableFactory.super.getOrder() + 100;
  }

}
