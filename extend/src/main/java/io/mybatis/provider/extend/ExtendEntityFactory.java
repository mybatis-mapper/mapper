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

package io.mybatis.provider.extend;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通过 SPI 工厂扩展 EntityColumn 和 EntityTable
 *
 * @author liuzh
 */
public class ExtendEntityFactory extends EntityFactory {
  public static final int ORDER = DEFAULT_ORDER + 10;

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    return new ExtendEntityTable(next().createEntityTable(entityClass));
  }

  @Override
  public void assembleEntityColumns(EntityTable entityTable) {
    next().assembleEntityColumns(entityTable);
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityField field) {
    Optional<List<EntityColumn>> optionalEntityColumns = next().createEntityColumn(field);
    if (optionalEntityColumns.isPresent()) {
      return optionalEntityColumns.map(columns -> columns.stream().map(ExtendEntityColumn::new).collect(Collectors.toList()));
    } else if (field.isAnnotationPresent(Extend.Column.class)) {
      Extend.Column column = field.getAnnotation(Extend.Column.class);
      String columnName = column.value();
      if (columnName.isEmpty()) {
        columnName = field.getName();
      }
      return Optional.of(Arrays.asList(new ExtendEntityColumn(new EntityColumn(field, columnName, column.id()))));
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

}
