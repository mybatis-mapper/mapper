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

import java.util.List;

/**
 * 包
 *
 * @author liuzh
 */
public class Package extends Base {
  /**
   * 子包
   */
  private List<Package>  packages;
  /**
   * 模板
   */
  private List<Template> templates;

  public List<Package> getPackages() {
    return packages;
  }

  public void setPackages(List<Package> packages) {
    this.packages = packages;
  }

  public List<Template> getTemplates() {
    return templates;
  }

  public void setTemplates(List<Template> templates) {
    this.templates = templates;
  }
}
