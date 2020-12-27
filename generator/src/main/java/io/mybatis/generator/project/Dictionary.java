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
 * 目录
 *
 * @author liuzh
 */
public class Dictionary extends Base {
  /**
   * 子目录，包和目录不能同时有
   */
  private List<Dictionary> dictionaries;
  /**
   * 子包，包和目录不能同时有
   */
  private List<Package>    packages;
  /**
   * 文件、模板
   */
  private List<Template>   templates;

  public List<Dictionary> getDictionaries() {
    return dictionaries;
  }

  public void setDictionaries(List<Dictionary> dictionaries) {
    this.dictionaries = dictionaries;
  }

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
