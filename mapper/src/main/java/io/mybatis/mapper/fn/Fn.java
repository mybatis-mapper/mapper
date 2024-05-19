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

package io.mybatis.mapper.fn;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityTable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参考通用 Mapper weekend 实现，用于获取方法引用对应的字段信息
 *
 * @author Frank
 * @author liuzh
 */
public interface Fn<T, R> extends Function<T, R>, Serializable {
  /**
   * 缓存方法引用和对应的列信息
   */
  Map<Fn<?, ?>, EntityColumn>           FN_COLUMN_MAP      = new ConcurrentHashMap<>();
  /**
   * 缓存方法引用和对应的字段信息
   */
  Map<Fn<?, ?>, Reflections.ClassField> FN_CLASS_FIELD_MAP = new ConcurrentHashMap<>();

  /**
   * 指定字段集合的虚拟表，当通过基类或者泛型基类获取字段时，需要设置字段所属的实体类
   *
   * @param entityClass 当使用基类获取泛型时，需要指定实体类类型
   * @param fns         指定字段
   * @return 虚拟表
   */
  @SafeVarargs
  static <E> Fns<E> of(Class<E> entityClass, Fn<E, Object>... fns) {
    return new Fns<>(entityClass, fns);
  }

  /**
   * 指定字段集合的虚拟表
   *
   * @param fns 指定字段
   * @return 虚拟表
   */
  @SafeVarargs
  static <E> Fns<E> of(Fn<E, Object>... fns) {
    return of(null, fns);
  }

  /**
   * 包含部分字段的虚拟表
   *
   * @param entityClass 实体类类型
   * @param columnNames 列名信息
   * @return 虚拟表
   */
  static <E> Fns<E> of(Class<E> entityClass, String... columnNames) {
    EntityTable entityTable = EntityFactory.create(entityClass);
    Set<String> columnNameSet = Arrays.stream(columnNames).collect(Collectors.toSet());
    List<EntityColumn> columns = entityTable.columns().stream().filter(column -> columnNameSet.contains(column.property())).collect(Collectors.toList());
    return new Fns<>(entityClass, entityTable.tableName(), columns);
  }

  /**
   * 指定类中字段名
   *
   * @param entityClass 字段所属实体类
   * @param field       实体类中的字段名
   */
  static <T> Fn<T, Object> field(Class<T> entityClass, Fn<T, Object> field) {
    return field.in(entityClass);
  }

  /**
   * 通过字符串形式指定（类中）字段名
   *
   * @param entityClass 字段所属实体类
   * @param field       实体类中的字段名
   */
  static <T> Fn<T, Object> field(Class<T> entityClass, String field) {
    return new FnName<>(entityClass, field);
  }

  /**
   * 通过字符串形式指定（表中的）列名
   *
   * @param entityClass 字段所属实体类
   * @param column      实体类对应表中的列名
   */
  static <T> Fn<T, Object> column(Class<T> entityClass, String column) {
    return new FnName<>(entityClass, column, true);
  }

  /**
   * 当前字段所属的实体类，当实体存在继承关系时
   * 父类的方法引用无法获取字段所属的实体类，需要通过该方法指定
   *
   * @param entityClass 指定实体类
   * @return 带有指定实体类的 Fn
   */
  default Fn<T, R> in(Class<?> entityClass) {
    return new FnImpl<>(this, entityClass);
  }

  /**
   * 转换为字段：获取方法引用对应的字段信息
   *
   * @return 方法引用对应的字段信息
   */
  default String toField() {
    return toClassField().getField();
  }

  /**
   * 转换为字段对应的列信息：获取方法引用对应的列信息
   *
   * @return 方法引用对应的列信息
   */
  default String toColumn() {
    return toEntityColumn().column();
  }

  /**
   * 获取字段信息
   *
   * @return 字段名和所在类信息
   */
  default Reflections.ClassField toClassField() {
    return FN_CLASS_FIELD_MAP.computeIfAbsent(this, key -> Reflections.fnToFieldName(key));
  }

  /**
   * 转换为字段对应的列信息：获取方法引用对应的列信息
   *
   * @return 方法引用对应的列信息
   */
  default EntityColumn toEntityColumn() {
    return FN_COLUMN_MAP.computeIfAbsent(this, key -> {
      Reflections.ClassField classField = toClassField();
      List<EntityColumn> columns = EntityFactory.create(classField.getClazz()).columns();
      return columns.stream()
          // 先区分大小写匹配字段
          .filter(column -> column.property().equals(classField.getField())).findFirst()
          // 如果不存在，再忽略大小写进行匹配
          .orElseGet(() -> columns.stream().filter(classField).findFirst()
              .orElseThrow(() -> new RuntimeException(classField.getField()
                  + " does not mark database column field annotations, unable to obtain column information")));
    });
  }

  /**
   * 带有指定类型的方法引用
   */
  class FnImpl<T, R> implements Fn<T, R> {

    final Fn<T, R> fn;
    final Class<?> entityClass;

    public FnImpl(Fn<T, R> fn, Class<?> entityClass) {
      this.fn = fn;
      this.entityClass = entityClass;
    }

    @Override
    public R apply(T t) {
      return fn.apply(t);
    }

  }

  /**
   * 间接支持直接指定字段名或列名，避免只能通过方法引用使用
   */
  class FnName<T, R> implements Fn<T, R> {
    final Class<?> entityClass;
    final String   name;
    /**
     * false代表name为字段，true代表name值为列
     */
    final boolean  column;

    public FnName(Class<?> entityClass, String name, boolean column) {
      this.entityClass = entityClass;
      this.name = name;
      this.column = column;
    }

    public FnName(Class<?> entityClass, String name) {
      this(entityClass, name, false);
    }

    @Override
    public Fn<T, R> in(Class<?> entityClass) {
      return new FnName<>(entityClass, this.name, this.column);
    }

    @Override
    public R apply(Object o) {
      return null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      FnName<?, ?> fnName = (FnName<?, ?>) o;
      return column == fnName.column && Objects.equals(entityClass, fnName.entityClass) && Objects.equals(name, fnName.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(entityClass, name, column);
    }
  }

  /**
   * 字段数组，用于获取字段对应的所有字段名和列名，当前对象相当于一个部分字段的虚拟表
   *
   * @param <E> 实体类型
   */
  class Fns<E> extends EntityTable {

    /**
     * 通过直接值设置
     *
     * @param entityClass 实体类类型
     * @param table       表名
     * @param columns     列集合
     */
    private Fns(Class<E> entityClass, String table, List<EntityColumn> columns) {
      super(entityClass);
      this.table = table;
      this.columns = columns;
    }

    /**
     * 通过字段设置
     *
     * @param fns 字段数组
     */
    @SafeVarargs
    private Fns(Class<E> entityClass, Fn<E, Object>... fns) {
      super(entityClass);
      this.columns = new ArrayList<>(fns.length);
      for (int i = 0; i < fns.length; i++) {
        if (entityClass != null) {
          this.columns.add(fns[i].in(entityClass).toEntityColumn());
        } else {
          this.columns.add(fns[i].toEntityColumn());
        }
        if (i == 0) {
          EntityTable entityTable = this.columns.get(i).entityTable();
          this.table = entityTable.tableName();
          this.style = entityTable.style();
          this.entityClass = entityTable.entityClass();
          this.resultMap = entityTable.resultMap();
          this.autoResultMap = entityTable.autoResultMap();
        }
      }
    }

    /**
     * 当前表字段是否为空，为空则没有设置任何字段
     *
     * @return
     */
    public boolean isNotEmpty() {
      return !columns().isEmpty();
    }

  }
}
