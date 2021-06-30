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

import io.mybatis.provider.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 通过 SPI 工厂扩展 EntityColumn 和 EntityTable
 *
 * @author liuzh
 */
public class JpaEntityTableFactory implements EntityTableFactory {

  @Override
  public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
    EntityTable entityTable = chain.createEntityTable(entityClass);
    if (entityClass.isAnnotationPresent(Table.class)) {
      Table table = entityClass.getAnnotation(Table.class);
      if (!table.name().isEmpty()) {
        entityTable.table(table.name());
      }
    }
    return entityTable;
  }

  @Override
  public int getOrder() {
    return EntityTableFactory.super.getOrder() + 100;
  }

}
