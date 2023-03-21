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

package io.mybatis.common.core;

import io.mybatis.common.util.I18n;

/**
 * 响应码
 *
 * @author liuzh
 */
public class Code {
  public static final String        CODE_BUNDLE    = "mybatis_common_code";
  public static final I18n.Language LANG           = I18n.language(CODE_BUNDLE);
  public static final Code          SUCCESS        = new Code("00000");
  public static final Code          FAILURE        = new Code("M0100");
  public static final Code          UNKNOWN        = new Code("M0200");
  public static final Code          SAVE_FAILURE   = new Code("M0201");
  public static final Code          UPDATE_FAILURE = new Code("M0202");
  public static final Code          DELETE_FAILURE = new Code("M0203");

  private String code;
  private String message;

  public Code() {
  }

  public Code(String code) {
    this.code = code;
    this.message = LANG.message(code);
  }

  public Code(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
