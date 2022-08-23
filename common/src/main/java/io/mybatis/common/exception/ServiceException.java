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

package io.mybatis.common.exception;

import io.mybatis.common.core.Code;

/**
 * 业务异常
 *
 * @author liuzh
 */
public class ServiceException extends RuntimeException {
  private Code code;

  public ServiceException(Code code) {
    super(code.getMessage());
    this.code = code;
  }

  public ServiceException(Code code, Throwable cause) {
    this(code);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }

  public void setCode(Code code) {
    this.code = code;
  }
}
