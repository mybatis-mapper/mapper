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

package io.mybatis.mapper.fn;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考通用 Mapper weekend 实现，用于获取方法引用对应的字段信息
 *
 * @author Frank
 * @author liuzh
 */
public class Reflections {
  private static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");
  private static final Pattern IS_PATTERN                 = Pattern.compile("^is[A-Z].*");
  private static final Pattern INSTANTIATED_CLASS_PATTERN = Pattern.compile("\\(L(?<cls>.+);\\).+");

  private Reflections() {
  }

  public static ClassField fnToFieldName(Fn fn) {
    try {
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
      //主要是这里  serializedLambda.getInstantiatedMethodType()
      Matcher matcher = INSTANTIATED_CLASS_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
      String implClass;
      if (matcher.find()) {
        implClass = matcher.group("cls").replaceAll("/", "\\.");
      } else {
        implClass = serializedLambda.getImplClass().replaceAll("/", "\\.");
      }
      Class<?> clazz = Class.forName(implClass);
      return new ClassField(clazz, field);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static class ClassField {
    private final Class<?> clazz;
    private final String   field;

    public ClassField(Class<?> clazz, String field) {
      this.clazz = clazz;
      this.field = field;
    }

    public Class<?> getClazz() {
      return clazz;
    }

    public String getField() {
      return field;
    }
  }
}
