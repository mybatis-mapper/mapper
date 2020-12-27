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

package io.mybatis.common.util;

import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多语言支持
 * <p>
 * 测试时，通过 JVM 参数可以设置 Locale {@code -Duser.country=US -Duser.language=en}
 *
 * @author liuzh
 */
public class I18n {
  public static volatile Map<String, ResourceBundle> RESOURCE_BUNDLE_MAP = new ConcurrentHashMap<>();

  /**
   * 获取对应语言的文本，当资源文件或key不存在时，直接返回 {@code MessageFormat.format(key, args)}
   *
   * @param bundleName 资源文件名
   * @param key        字符串key
   * @param args       格式化参数
   * @return 格式化文本
   */
  public static String message(String bundleName, String key, Object... args) {
    if (!RESOURCE_BUNDLE_MAP.containsKey(bundleName)) {
      synchronized (RESOURCE_BUNDLE_MAP) {
        if (!RESOURCE_BUNDLE_MAP.containsKey(bundleName)) {
          ResourceBundle bundle;
          try {
            bundle = ResourceBundle.getBundle(bundleName);
          } catch (Exception e) {
            bundle = null;
          }
          RESOURCE_BUNDLE_MAP.put(bundleName, bundle);
        }
      }
    }
    ResourceBundle bundle = RESOURCE_BUNDLE_MAP.get(bundleName);
    if (bundle == null) {
      return MessageFormat.format(key, args);
    }
    try {
      return MessageFormat.format(bundle.getString(key), args);
    } catch (MissingResourceException e) {
      return MessageFormat.format(key, args);
    }
  }

}
