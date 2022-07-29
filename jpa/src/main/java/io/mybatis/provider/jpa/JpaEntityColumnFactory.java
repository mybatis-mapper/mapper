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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 通过 SPI 工厂扩展 EntityColumn 和 EntityTable
 *
 * @author liuzh
 */
public class JpaEntityColumnFactory implements EntityColumnFactory {

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field, Chain chain) {
    Optional<List<EntityColumn>> optionalEntityColumns = chain.createEntityColumn(entityTable, field);
    if (field.isAnnotationPresent(Transient.class)) {
      return Optional.empty();
    } else if (!optionalEntityColumns.isPresent()) {
      //没有 @Transient 注解的字段都认为是表字段，不自动排除字段，字段名默认驼峰转下划线
      optionalEntityColumns = Optional.of(Arrays.asList(EntityColumn.of(field).column(Style.getDefaultStyle().columnName(entityTable, field))));
    }
    if (optionalEntityColumns.isPresent()) {
      List<EntityColumn> entityColumns = optionalEntityColumns.get();
      for (EntityColumn entityColumn : entityColumns) {
        EntityField entityField = entityColumn.field();
        //主键
        if (!entityColumn.id()) {
          entityColumn.id(entityField.isAnnotationPresent(Id.class));
        }
        //列名
        if (field.isAnnotationPresent(Column.class)) {
          Column column = field.getAnnotation(Column.class);
          String columnName = column.name();
          if (!columnName.isEmpty()) {
            entityColumn.column(columnName);
          }
          entityColumn.insertable(column.insertable()).updatable(column.updatable());
          if (column.scale() != 0) {
            entityColumn.numericScale(String.valueOf(column.scale()));
          }
        }
        //只能默认空 ASC，或者写 ASC 或 DESC，不能写多个列
        if (field.isAnnotationPresent(OrderBy.class)) {
          OrderBy orderBy = field.getAnnotation(OrderBy.class);
          if (orderBy.value().isEmpty()) {
            entityColumn.orderBy("ASC");
          } else {
            entityColumn.orderBy(orderBy.value());
          }
        }
      }
    }
    return optionalEntityColumns;
  }

  @Override
  public int getOrder() {
    return EntityColumnFactory.super.getOrder() + 100;
  }

}
