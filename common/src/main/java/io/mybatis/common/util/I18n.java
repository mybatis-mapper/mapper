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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 多语言支持
 * <p>
 * 测试时，通过 JVM 参数可以设置 Locale {@code -Duser.country=US -Duser.language=en}
 *
 * @author liuzh
 */
public class I18n {

  /**
   * 获取对应语言的文本，当资源文件或key不存在时，直接返回 {@code MessageFormat.format(key, args)}
   *
   * @param locale     语言
   * @param bundleName 资源文件名
   * @param key        字符串key
   * @param args       格式化参数
   * @return 格式化文本
   */
  public static String message(Locale locale, String bundleName, String key, Object... args) {
    ResourceBundle bundle;
    try {
      bundle = ResourceBundle.getBundle(bundleName, locale);
    } catch (Exception e) {
      bundle = null;
    }
    try {
      return MessageFormat.format(bundle.getString(key), args);
    } catch (MissingResourceException e) {
      return MessageFormat.format(key, args);
    }
  }

  /**
   * 获取对应语言的文本，当资源文件或key不存在时，直接返回 {@code MessageFormat.format(key, args)}
   *
   * @param bundleName 资源文件名
   * @param key        字符串key
   * @param args       格式化参数
   * @return 格式化文本
   */
  public static String message(String bundleName, String key, Object... args) {
    return message(Locale.getDefault(), bundleName, key, args);
  }

  /**
   * 获取语言包
   *
   * @param locale     语言
   * @param bundleName 语言包名称
   * @return
   */
  public static Language language(Locale locale, String bundleName) {
    return (key, args) -> message(locale, bundleName, key, args);
  }

  /**
   * 获取语言包
   *
   * @param bundleName 语言包名称
   * @return
   */
  public static Language language(String bundleName) {
    return language(Locale.getDefault(), bundleName);
  }

  /**
   * 语言包
   */
  public interface Language {
    /**
     * 获取对应语言的文本
     *
     * @param key
     * @param args
     * @return
     */
    String message(String key, Object... args);
  }

}
