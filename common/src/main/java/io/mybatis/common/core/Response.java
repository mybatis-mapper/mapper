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

import io.mybatis.common.exception.ServiceException;
import io.mybatis.common.util.I18n;

/**
 * 响应结果
 *
 * @author liuzh
 */
public class Response<T extends Response> {
  public static final String  RESPONSE_BUNDLE = "mybatis_common_response";
  /**
   * 执行是否成功
   */
  protected           boolean success;
  /**
   * 响应码
   */
  protected           String  code;
  /**
   * 响应信息
   */
  protected           String  message;

  public static Response ok() {
    Response response = new Response();
    response.success = true;
    response.code = Code.SUCCESS.getCode();
    return response;
  }

  public static Response error() {
    return error(Code.UNKONWN);
  }

  public static Response error(String code) {
    return error(code, I18n.message(RESPONSE_BUNDLE, code));
  }

  public static Response error(Throwable t) {
    return error(Code.UNKONWN.getCode(), t.getMessage());
  }

  public static Response error(ServiceException e) {
    return error(e.getCode());
  }

  public static Response error(String code, String message) {
    Response response = new Response();
    response.success = false;
    response.code = code;
    response.message = message;
    return response;
  }

  public static Response error(Code code) {
    return error(code.getCode(), code.getMessage());
  }

  public T code(String code) {
    this.code = code;
    return (T) this;
  }

  public T message(String message) {
    this.message = message;
    return (T) this;
  }

  public T success(boolean success) {
    this.success = success;
    return (T) this;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
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
