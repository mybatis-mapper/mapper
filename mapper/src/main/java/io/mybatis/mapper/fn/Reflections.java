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

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考通用 Mapper weekend 实现，用于获取方法引用对应的字段信息
 *
 * @author Frank
 * @author liuzh
 */
public class Reflections {
  private static final Pattern GET_PATTERN                = Pattern.compile("^get[A-Z].*");
  private static final Pattern IS_PATTERN                 = Pattern.compile("^is[A-Z].*");
  private static final Pattern INSTANTIATED_CLASS_PATTERN = Pattern.compile("\\(L(?<cls>.+);\\).+");

  private Reflections() {
  }

  public static ClassField fnToFieldName(Fn<?, ?> fn) {
    try {
      Class<?> clazz = null;
      //支持直接指定的字段名
      if (fn instanceof Fn.FnName) {
        Fn.FnName<?, ?> field = (Fn.FnName<?, ?>) fn;
        if (field.column) {
          return new ClassColumn(field.entityClass, field.name);
        } else {
          return new ClassField(field.entityClass, field.name);
        }
      }
      //支持指定实体类
      if (fn instanceof Fn.FnImpl) {
        clazz = ((Fn.FnImpl<?, ?>) fn).entityClass;
        fn = ((Fn.FnImpl<?, ?>) fn).fn;
        //避免嵌套多次的情况
        while (fn instanceof Fn.FnImpl) {
          fn = ((Fn.FnImpl<?, ?>) fn).fn;
        }
      }
      Method method = fn.getClass().getDeclaredMethod("writeReplace");
      method.setAccessible(Boolean.TRUE);
      SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
      String getter = serializedLambda.getImplMethodName();
      if (GET_PATTERN.matcher(getter).matches()) {
        getter = getter.substring(3);
      } else if (IS_PATTERN.matcher(getter).matches()) {
        getter = getter.substring(2);
      }
      String field = Introspector.decapitalize(getter);
      if (clazz == null) {
        //主要是这里  serializedLambda.getInstantiatedMethodType()
        Matcher matcher = INSTANTIATED_CLASS_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
        String implClass;
        if (matcher.find()) {
          implClass = matcher.group("cls").replaceAll("/", "\\.");
        } else {
          implClass = serializedLambda.getImplClass().replaceAll("/", "\\.");
        }
        clazz = Class.forName(implClass);
      }
      return new ClassField(clazz, field);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 记录字段对应的类和字段名
   */
  public static class ClassField implements Predicate<EntityColumn> {
    private final Class<?> clazz;
    private final String   field;

    public ClassField(Class<?> clazz, String field) {
      this.clazz = clazz;
      this.field = field;
    }

    @Override
    public boolean test(EntityColumn column) {
      return getField().equalsIgnoreCase(column.property());
    }

    public Class<?> getClazz() {
      return clazz;
    }

    public String getField() {
      return field;
    }
  }

  /**
   * 记录字段对应的类和列名
   */
  public static class ClassColumn extends ClassField {

    public ClassColumn(Class<?> clazz, String field) {
      super(clazz, field);
    }

    @Override
    public boolean test(EntityColumn column) {
      return getField().equalsIgnoreCase(column.column());
    }
  }
}
