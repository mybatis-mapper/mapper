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

package io.mybatis.generator.project;

import java.util.Map;
import java.util.Optional;

/**
 * 基础信息
 *
 * @author liuzh
 */
public class Base {
  /**
   * 名称
   */
  protected String              name;
  /**
   * 附加信息
   */
  protected Map<String, String> attrs;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Optional<String> attr(String attr) {
    if (attrs != null) {
      return Optional.ofNullable(attrs.get(attr));
    }
    return Optional.empty();
  }

  public Map<String, String> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, String> attrs) {
    this.attrs = attrs;
  }
}
