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

package io.mybatis.common.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 工具类，主要判断空和非空，以及true和false
 *
 * @author liuzh
 */
public class Utils {

  /**
   * 字符串是否为空，空的定义如下:<br>
   * 1、为null <br>
   * 2、为""<br>
   *
   * @param str 被检测的字符串
   * @return 是否为空
   */
  public static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  /**
   * 字符串是否为非空白，非空白的定义如下： <br>
   * 1、不为null <br>
   * 2、不为""<br>
   *
   * @param str 被检测的字符串
   * @return 是否为非空
   */
  public static boolean isNotEmpty(CharSequence str) {
    return !isEmpty(str);
  }

  /**
   * 字符串是否为空白 空白的定义如下： <br>
   * 1、为null <br>
   * 2、为不可见字符（如空格）<br>
   * 3、""<br>
   *
   * @param str 被检测的字符串
   * @return 是否为空
   */
  public static boolean isBlank(CharSequence str) {
    int length;

    if ((str == null) || ((length = str.length()) == 0)) {
      return true;
    }

    for (int i = 0; i < length; i++) {
      // 只要有一个非空字符即为非空字符串
      if (!isBlankChar(str.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * 是否空白符<br>
   * 空白符包括空格、制表符、全角空格和不间断空格<br>
   *
   * @param c 字符
   * @return 是否空白符
   * @see Character#isWhitespace(int)
   * @see Character#isSpaceChar(int)
   * @since 4.0.10
   */
  public static boolean isBlankChar(int c) {
    return Character.isWhitespace(c)
        || Character.isSpaceChar(c)
        || c == '\ufeff'
        || Character.getType(c) == Character.FORMAT;
  }

  /**
   * 字符串是否为非空白，非空白的定义如下： <br>
   * 1、不为null <br>
   * 2、不为不可见字符（如空格）<br>
   * 3、不为""<br>
   *
   * @param str 被检测的字符串
   * @return 是否为非空
   */
  public static boolean isNotBlank(CharSequence str) {
    return !isBlank(str);
  }

  /**
   * 数组是否为空
   *
   * @param <T>   数组元素类型
   * @param array 数组
   * @return 是否为空
   */
  public static <T> boolean isEmpty(T[] array) {
    return array == null || array.length == 0;
  }

  /**
   * 数组是否为非空
   *
   * @param <T>   数组元素类型
   * @param array 数组
   * @return 是否为非空
   */
  public static <T> boolean isNotEmpty(T[] array) {
    return !isEmpty(array);
  }

  /**
   * 数组是否为空
   *
   * @param <T>        数组元素类型
   * @param collection 集合
   * @return 是否为空
   */
  public static <T> boolean isEmpty(Collection<T> collection) {
    return collection == null || collection.size() == 0;
  }

  /**
   * 数组是否为非空
   *
   * @param <T>        数组元素类型
   * @param collection 集合
   * @return 是否为非空
   */
  public static <T> boolean isNotEmpty(Collection<T> collection) {
    return !isEmpty(collection);
  }

  /**
   * 判断对象是否为空
   *
   * @param obj 对象
   * @return 是否为空
   */
  public static boolean isEmpty(Object obj) {
    if (Objects.isNull(obj)) {
      return true;
    } else if (obj instanceof Optional) {
      return !((Optional<?>) obj).isPresent();
    } else if (obj instanceof CharSequence) {
      return ((CharSequence) obj).length() == 0;
    } else if (obj.getClass().isArray()) {
      return Array.getLength(obj) == 0;
    } else if (obj instanceof Collection) {
      return ((Collection<?>) obj).isEmpty();
    } else {
      return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
    }
  }

}
