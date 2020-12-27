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

package io.mybatis.database;

import io.mybatis.common.util.Utils;

public class IntrospectedBase {
  protected String name;
  protected String remarks;

  /**
   * 根据条件进行过滤
   *
   * @param searchText
   * @param searchComment
   * @param matchType
   * @param caseSensitive
   * @return
   */
  public boolean filter(String searchText, String searchComment, MatchType matchType, boolean caseSensitive) {
    if (Utils.isNotEmpty(searchText)) {
      if (matchType == MatchType.EQUALS) {
        if (caseSensitive) {
          if (!getName().equals(searchText)) {
            return false;
          }
        } else {
          if (!getName().equalsIgnoreCase(searchText)) {
            return false;
          }
        }
      } else {
        if (caseSensitive) {
          if (getName().indexOf(searchText) == -1) {
            return false;
          }
        } else {
          if (getName().toUpperCase().indexOf(searchText.toUpperCase()) == -1) {
            return false;
          }
        }
      }
    }
    if (Utils.isNotEmpty(searchComment)) {
      if (matchType == MatchType.EQUALS) {
        if (caseSensitive) {
          return getRemarks() != null && getRemarks().equals(searchComment);
        } else {
          return getRemarks() != null && getRemarks().equalsIgnoreCase(searchComment);
        }
      } else {
        if (caseSensitive) {
          return getRemarks() != null && getRemarks().indexOf(searchComment) != -1;
        } else {
          return getRemarks() != null && getRemarks().indexOf(searchComment) != -1;
        }
      }
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
